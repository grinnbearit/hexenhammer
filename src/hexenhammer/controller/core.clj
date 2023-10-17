(ns hexenhammer.controller.core
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.core :as l]
            [hexenhammer.logic.unit :as lu]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.movement :as lm]
            [hexenhammer.controller.dice :as cd]
            [hexenhammer.controller.unit :as cu]
            [hexenhammer.controller.event :as ce]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.battlemap :as cb]))


(defmulti select (fn [state cube] [(:game/phase state) (:game/subphase state)]))


(defmethod select [:setup :select-hex]
  [state cube]
  (let [entity-class (get-in state [:game/battlefield cube :entity/class])
        to-subphase ({:terrain :add-unit :unit :remove-unit} entity-class)]

    (-> (assoc state :game/subphase to-subphase)
        (assoc :game/selected cube)
        (update :game/battlemap l/set-state [cube] :selected))))


(defmulti unselect :game/phase)


(defmethod unselect :setup
  [state]
  (let [cube (:game/selected state)]
    (-> (assoc state :game/subphase :select-hex)
        (update :game/battlemap l/set-state [cube] :silent-selectable)
        (dissoc :game/selected))))


(defmethod select [:setup :add-unit]
  [state cube]
  (if (= cube (:game/selected state))
    (unselect state)
    (-> (unselect state)
        (select cube))))


(defmethod select [:setup :remove-unit]
  [state cube]
  (if (= cube (:game/selected state))
    (unselect state)
    (-> (unselect state)
        (select cube))))


(defn add-unit
  [state player facing {:keys [M Ld R]}]
  (let [cube (:game/selected state)
        prev-id (or (get-in state [:game/units player "infantry" :counter]) 0)
        next-id (inc prev-id)
        unit (me/gen-infantry cube player next-id facing :M M :Ld Ld :R R)]
    (-> (update-in state [:game/battlefield cube] lt/swap unit)
        (assoc-in [:game/units player "infantry" :cubes next-id] cube)
        (assoc-in [:game/units player "infantry" :counter] next-id)
        (cb/refresh-battlemap [cube])
        (update :game/battlemap l/set-state [cube] :selectable)
        (unselect))))


(defn remove-unit
  [state]
  (let [unit-cube (:game/selected state)
        {:keys [unit/player entity/name unit/id]} (get-in state [:game/battlefield unit-cube])]
    (-> (update state :game/battlefield lu/remove-unit unit-cube)
        (update-in [:game/units player name :cubes] dissoc id)
        (cb/refresh-battlemap [unit-cube])
        (update :game/battlemap l/set-state [unit-cube] :selectable)
        (unselect))))


(defn swap-terrain
  [state terrain]
  (let [cube (:game/selected state)
        entity (get-in state [:game/battlefield cube])
        new-terrain (case terrain
                      :open (me/gen-open-ground cube)
                      :dangerous (me/gen-dangerous-terrain cube)
                      :impassable (me/gen-impassable-terrain cube))
        new-entity (if (le/terrain? entity) new-terrain (lt/place new-terrain entity))]
    (-> (assoc-in state [:game/battlefield cube] new-entity)
        (cb/refresh-battlemap [cube])
        (unselect))))


(defmulti trigger-event (fn [state event] (:game/phase state)))


(defn trigger
  "pops the next event in the queue, if empty triggers the stored callback
  callback needs to set a mainphase and subphase"
  ([state]
   (if-let [event (peek (:game/events state))]
     (-> (update state :game/events pop)
         (update :game/trigger dissoc :event)
         (dissoc :game/battlemap)
         (assoc :game/phase (:event/class event)
                :game/subphase :start)
         (trigger-event event))
     (let [callback (get-in state [:game/trigger :callback])]
       (-> (dissoc state
                   :game/phase :game/subphase
                   :game/battlemap :game/trigger)
           (callback)))))
  ([state callback]
   (-> (assoc-in state [:game/trigger :callback] callback)
       (trigger))))


(defmethod trigger-event :dangerous
  [state event]
  (let [{:keys [event/cube event/unit-key]} event]
    (if-let [unit-cube (cu/key->cube state unit-key)]
      (let [unit (get-in state [:game/battlefield unit-cube])
            models (mu/models unit)
            roll (cd/roll! models)
            models-destroyed (cd/matches roll 1)
            unit-destroyed? (<= models models-destroyed)]
        (-> (if unit-destroyed?
              (cu/destroy-unit state unit-cube)
              (cu/destroy-models state unit-cube cube models-destroyed))
            (assoc-in [:game/trigger :event]
                      {:unit unit
                       :models-destroyed models-destroyed
                       :unit-destroyed? unit-destroyed?
                       :roll roll})
            (cb/refresh-battlemap [cube unit-cube])
            (update :game/battlemap l/set-state [cube unit-cube] :marked)))
      (trigger state))))


(defmethod trigger-event :opportunity-attack
  [state event]
  (let [{:keys [event/cube event/unit-key event/wounds]} event]
    (if-let [unit-cube (cu/key->cube state unit-key)]
      (let [unit (get-in state [:game/battlefield unit-cube])
            unit-wounds (mu/wounds unit)
            unit-destroyed? (<= unit-wounds wounds)]
        (-> (if unit-destroyed?
              (cu/destroy-unit state unit-cube)
              (cu/damage-unit state unit-cube cube wounds))
            (assoc-in [:game/trigger :event]
                      {:unit unit
                       :wounds wounds
                       :unit-destroyed? unit-destroyed?})
            (cb/refresh-battlemap [cube unit-cube])
            (update :game/battlemap l/set-state [cube unit-cube] :marked)))
      (trigger state))))


(defmethod trigger-event :heavy-casualties
  [state event]
  (let [{:keys [event/cube event/unit-key]} event]
    (if-let [unit-cube (cu/key->cube state unit-key)]
      (if-let [panickable? (lu/panickable? (:game/battlefield state) unit-cube)]
        (let [unit (get-in state [:game/battlefield unit-cube])
              roll (cd/roll! 2)
              passed? (<= (apply + roll) (:unit/Ld unit))]
          (-> (if passed?
                (assoc state :game/subphase :passed)
                (assoc state :game/subphase :failed))
              (assoc-in [:game/battlefield unit-cube :unit/phase :panicked?] true)
              (assoc-in [:game/trigger :event]
                        {:trigger-cube cube
                         :unit-cube unit-cube
                         :roll roll})
              (cb/refresh-battlemap [cube unit-cube])
              (update :game/battlemap l/set-state [cube unit-cube] :marked)))
        (trigger state))
      (trigger state))))


(defmethod trigger-event :panic
  [state event]
  (let [{:keys [event/cube event/unit-key]} event]
    (if-let [unit-cube (cu/key->cube state unit-key)]
      (if-let [panickable? (lu/panickable? (:game/battlefield state) unit-cube)]
        (let [unit (get-in state [:game/battlefield unit-cube])
              roll (cd/roll! 2)
              passed? (<= (apply + roll) (:unit/Ld unit))]
          (-> (if passed?
                (assoc state :game/subphase :passed)
                (assoc state :game/subphase :failed))
              (assoc-in [:game/battlefield unit-cube :unit/phase :panicked?] true)
              (assoc-in [:game/trigger :event]
                        {:unit-cube unit-cube
                         :roll roll})
              (cb/refresh-battlemap [cube unit-cube])
              (update :game/battlemap l/set-state [cube unit-cube] :marked)))
        (trigger state))
      (trigger state))))


(defn reset-charge
  [{:keys [game/player game/battlefield] :as state}]
  (let [player-cubes (cu/unit-cubes state player)
        charger-cubes (filter #(lm/charger? battlefield %) player-cubes)]
    (-> (assoc state
               :game/phase :charge
               :game/subphase :select-hex
               :game/charge {:chargers (set charger-cubes)})
        (cb/refresh-battlemap charger-cubes)
        (update :game/battlemap l/set-state charger-cubes :selectable))))


(defn to-charge
  [state]
  (let [unit-cubes (cu/unit-cubes state)]
    (-> (update state :game/battlefield lu/phase-reset unit-cubes)
        (reset-charge))))


(defmethod unselect :charge
  [state]
  (let [charger-cubes (get-in state [:game/charge :chargers])]
    (-> (assoc state
               :game/subphase :select-hex
               :game/charge {:chargers charger-cubes})
        (dissoc :game/selected
                :game/battlemap)
        (cb/refresh-battlemap charger-cubes)
        (update :game/battlemap l/set-state charger-cubes :selectable))))


(defmethod select [:charge :select-hex]
  [state cube]
  (if (or (= cube (:game/selected state))
          (= cube (get-in state [:game/charge :pointer :cube])))
    (unselect state)
    (let [{:keys [battlemap breadcrumbs pointer->events pointer->targets pointer->range]} (lm/show-charge (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/subphase :select-target
                 :game/selected cube
                 :game/battlemap battlemap)
          (update :game/charge assoc
                  :battlemap battlemap
                  :breadcrumbs breadcrumbs
                  :pointer->events pointer->events
                  :pointer->targets pointer->targets
                  :pointer->range pointer->range)))))


(defmethod select [:charge :select-target]
  [state cube]
  (-> (assoc state :game/subphase :select-hex)
      (select cube)))


(defmethod select [:charge :declare]
  [state cube]
  (-> (assoc state :game/subphase :select-hex)
      (select cube)))


(defmethod unselect :react
  [state]
  (let [target-cubes (get-in state [:game/react :targets])]
    (-> (assoc state
               :game/subphase :select-hex
               :game/react {:targets target-cubes})
        (dissoc :game/selected
                :game/battlemap)
        (cb/refresh-battlemap target-cubes)
        (update :game/battlemap l/set-state target-cubes :selectable))))


(defn declare-charge
  [state]
  (let [pointer (get-in state [:game/charge :pointer])
        target-cubes (get-in state [:game/charge :pointer->targets pointer])
        target-units (map (:game/battlefield state) target-cubes)
        target-keys (map mu/unit-key target-units)]
    (-> (dissoc state :game/charge)
        (assoc :game/phase :react
               :game/react {:charger (:game/selected state)
                            :declared (set target-keys)
                            :targets (set target-cubes)})
        (unselect))))


(defmethod select [:react :select-hex]
  [state cube]
  (-> (assoc state :game/subphase :hold)
      (select cube)))


(defmethod select [:react :hold]
  [state cube]
  (if (= (:game/selected state) cube)
    (unselect state)
    (-> (assoc state :game/selected cube)
        (dissoc :game/battlemap)
        (cb/refresh-battlemap [cube])
        (update :game/battlemap l/set-state [cube] :selected))))


(defn react-hold
  [state]
  (let [cube (:game/selected state)]
    (-> (update-in state [:game/react :targets] disj cube)
        (unselect))))


(defn reset-movement
  [{:keys [game/player game/battlefield] :as state}]
  (let [player-cubes (cu/unit-cubes state player)
        movable-cubes (filter #(lm/movable? battlefield %) player-cubes)]
    (-> (assoc state
               :game/phase :movement
               :game/subphase :select-hex
               :game/movement {:movers (set movable-cubes)})
        (cb/refresh-battlemap movable-cubes)
        (update :game/battlemap l/set-state movable-cubes :selectable))))


(defn to-movement
  [state]
  (-> (dissoc state :game/charge)
      (reset-movement)))


(defmethod select [:movement :select-hex]
  [state cube]
  (-> (assoc state :game/subphase :reform)
      (select cube)))


(defmethod unselect :movement
  [state]
  (let [movable-cubes (get-in state [:game/movement :movers])]
    (-> (assoc state
               :game/subphase :select-hex
               :game/movement {:movers movable-cubes})
        (dissoc :game/selected
                :game/battlemap)
        (cb/refresh-battlemap movable-cubes)
        (update :game/battlemap l/set-state movable-cubes :selectable))))


(defn skip-movement
  [state]
  (let [unit-cube (:game/selected state)]
    (-> (assoc-in state [:game/battlefield unit-cube :unit/movement :unmoved?] true)
        (update-in [:game/movement :movers] disj unit-cube)
        (unselect))))


(defmulti move (fn [state pointer] [(:game/phase state) (:game/subphase state)]))


(defmethod move [:charge :select-target]
  [state pointer]
  (let [{:keys [battlemap breadcrumbs]} (:game/charge state)]
    (-> (assoc state
               :game/battlemap (merge battlemap (breadcrumbs pointer))
               :game/subphase :declare)
        (update :game/battlemap cm/set-mover-selected pointer)
        (assoc-in [:game/charge :pointer] pointer))))


(defmethod move [:charge :declare]
  [state pointer]
  (-> (assoc state :game/subphase :select-target)
      (move pointer)))


(defmethod move [:movement :reform]
  [state pointer]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        battlemap (get-in state [:game/movement :battlemap])]

    (-> (if (= (:facing pointer) (:unit/facing unit))
          (update state :game/movement dissoc :moved?)
          (assoc-in state [:game/movement :moved?] true))

        (assoc :game/battlemap battlemap)
        (assoc-in [:game/movement :pointer] pointer)
        (update :game/battlemap cm/set-mover-selected pointer))))


(defmethod move [:movement :forward]
  [state pointer]
  (cm/show-moves state pointer))


(defmethod move [:movement :reposition]
  [state pointer]
  (cm/show-moves state pointer))


(defmethod move [:movement :march]
  [state pointer]
  (cm/show-moves state pointer))


(defn finish-movement
  [state]
  (let [unit-cube (:game/selected state)
        pointer (get-in state [:game/movement :pointer])
        events (get-in state [:game/movement :pointer->events pointer])
        marched? (or (get-in state [:game/movement :marched?]) false)]

    (-> (update-in state [:game/battlefield unit-cube :unit/movement] assoc
                   :marched? marched?
                   :moved? true)
        (cu/move-unit unit-cube pointer)
        (update :game/events into events)
        (unselect)
        (trigger reset-movement))))


(defn movement-transition
  [state movement]
  (let [game-movement (select-keys (:game/movement state) [:movers])]
    (-> (assoc state
               :game/subphase movement
               :game/movement game-movement)
        (select (:game/selected state)))))


(defmethod select [:movement :reform]
  [state cube]
  (if (= cube (get-in state [:game/movement :pointer :cube]))
    (unselect state)
    (let [{:keys [battlemap pointer->events]} (lm/show-reform (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap)
          (update :game/movement assoc
                  :battlemap battlemap
                  :pointer->events pointer->events)
          (move pointer)))))


(defmethod select [:movement :forward]
  [state cube]
  (if (= cube (get-in state [:game/movement :pointer :cube]))
    (unselect state)
    (let [{:keys [battlemap breadcrumbs pointer->events]}  (lm/show-forward (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap)
          (update :game/movement assoc
                  :battlemap battlemap
                  :breadcrumbs breadcrumbs
                  :pointer->events pointer->events)
          (move pointer)))))


(defmethod select [:movement :reposition]
  [state cube]
  (if (= cube (get-in state [:game/movement :pointer :cube]))
    (unselect state)
    (let [{:keys [battlemap breadcrumbs pointer->events]}  (lm/show-reposition (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap)
          (update :game/movement assoc
                  :battlemap battlemap
                  :breadcrumbs breadcrumbs
                  :pointer->events pointer->events)
          (move pointer)))))


(defmethod select [:movement :march]
  [state cube]
  (if (= cube (get-in state [:game/movement :pointer :cube]))
    (unselect state)
    (let [{:keys [battlemap breadcrumbs pointer->events threats?]}  (lm/show-march (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))
          unit (get-in state [:game/battlefield cube])]

      (-> (assoc-in state [:game/movement :march]
                    (if threats?
                      (if (get-in unit [:unit/movement :marched?])
                        (if (get-in unit [:unit/movement :passed?]) :passed :failed)
                        :required)
                      :unnecessary))

          (assoc :game/selected cube
                 :game/battlemap battlemap)
          (update :game/movement assoc
                  :battlemap battlemap
                  :breadcrumbs breadcrumbs
                  :pointer->events pointer->events)
          (move pointer)))))


(defn test-march!
  [state]
  (let [cube (:game/selected state)
        pointer (get-in state [:game/movement :pointer])
        unit (get-in state [:game/battlefield cube])
        roll (cd/roll! 2)]
    (-> (assoc-in state [:game/battlefield cube :unit/movement]
                  {:marched? true
                   :roll roll
                   :passed? (<= (apply + roll) (:unit/Ld unit))})
        (unselect)
        (select cube)
        (movement-transition :march)
        (move pointer))))


(defmulti flee :game/phase)


(defmethod flee :heavy-casualties
  [state]
  (let [{:keys [unit-cube trigger-cube]} (get-in state [:game/trigger :event])
        unit (get-in state [:game/battlefield unit-cube])
        roll (cd/roll! 2)
        {:keys [battlemap end edge? events]} (lm/show-flee (:game/battlefield state)
                                                           unit-cube
                                                           trigger-cube
                                                           (apply + roll))]
    (-> (if edge?
          (cu/destroy-unit state unit-cube)
          (-> (assoc-in state [:game/battlefield unit-cube :unit/movement :fleeing?] true)
              (cu/move-unit unit-cube end)))
        (update :game/events into events)
        (assoc :game/battlemap battlemap
               :game/subphase :flee)
        (cb/refresh-battlemap [(:cube end)])
        (update :game/battlemap l/set-state [(:cube end)] :marked)
        (assoc-in [:game/trigger :event]
                  {:edge? edge?
                   :unit unit
                   :roll roll}))))


(defmethod flee :panic
  [state]
  (let [{:keys [unit-cube]} (get-in state [:game/trigger :event])
        unit (get-in state [:game/battlefield unit-cube])
        trigger-cube (ce/panic-trigger state unit)
        roll (cd/roll! 2)
        {:keys [battlemap end edge? events]} (lm/show-flee (:game/battlefield state)
                                                           unit-cube
                                                           trigger-cube
                                                           (apply + roll))]
    (-> (if edge?
          (cu/escape-unit state unit-cube (:cube end))
          (-> (assoc-in state [:game/battlefield unit-cube :unit/movement :fleeing?] true)
              (cu/move-unit unit-cube end)))
        (update :game/events into events)
        (assoc :game/battlemap battlemap
               :game/subphase :flee)
        (cb/refresh-battlemap [trigger-cube (:cube end)])
        (update :game/battlemap l/set-state [trigger-cube (:cube end)] :marked)
        (assoc-in [:game/trigger :event]
                  {:edge? edge?
                   :unit unit
                   :roll roll}))))
