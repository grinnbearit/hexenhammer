(ns hexenhammer.controller.core
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.core :as l]
            [hexenhammer.logic.unit :as lu]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.movement :as lm]
            [hexenhammer.controller.event :as ce]
            [hexenhammer.controller.dice :as cd]
            [hexenhammer.controller.unit :as cu]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.battlemap :as cb]))


(defmulti select (fn [state cube] [(:game/phase state) (:game/subphase state)]))


(defmethod select [:setup :select-hex]
  [state cube]
  (let [entity-class (get-in state [:game/battlefield cube :entity/class])
        to-subphase ({:terrain :add-unit :unit :remove-unit} entity-class)]

    (-> (assoc state :game/subphase to-subphase)
        (assoc :game/selected cube)
        (assoc-in [:game/battlefield cube :entity/state] :selected))))


(defmulti unselect (fn [state] (:game/phase state)))


(defmethod unselect :setup
  [state]
  (-> (assoc state :game/subphase :select-hex)
      (assoc-in [:game/battlefield (:game/selected state) :entity/state] :silent-selectable)
      (dissoc :game/selected)))


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
  [state player facing {:keys [M Ld]}]
  (let [cube (:game/selected state)
        terrain (get-in state [:game/battlefield cube])
        prev-id (or (get-in state [:game/units player "infantry" :counter]) 0)
        next-id (inc prev-id)
        unit (-> (me/gen-infantry cube player next-id facing :M M :Ld Ld)
                 (assoc :entity/state :selectable)
                 (lt/place terrain))]
    (-> (assoc-in state [:game/battlefield cube] unit)
        (assoc-in [:game/units player "infantry" :cubes next-id] cube)
        (assoc-in [:game/units player "infantry" :counter] next-id)
        (unselect))))


(defn remove-unit
  [state]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        terrain (-> (lt/pickup unit)
                    (assoc :entity/state :selectable))]
    (-> (assoc-in state [:game/battlefield cube] terrain)
        (update-in [:game/units (:unit/player unit) (:entity/name unit) :cubes] dissoc (:unit/id unit))
        (unselect))))


(defn swap-terrain
  [state terrain]
  (let [cube (:game/selected state)
        entity (get-in state [:game/battlefield cube])
        new-terrain (case terrain
                      :open (me/gen-open-ground cube)
                      :dangerous (me/gen-dangerous-terrain cube)
                      :impassable (me/gen-impassable-terrain cube))
        new-entity (if (le/terrain? entity) new-terrain (lt/place entity new-terrain))]
    (-> (assoc-in state [:game/battlefield cube] new-entity)
        (unselect))))


(defmulti trigger-event (fn [state event] (:game/phase state)))


(defn trigger
  "pops the next event in the queue, returns to the current main phase and sub phase if empty"
  [state]
  (if-let [event (peek (:game/events state))]
    (-> (ce/push-phase state)
        (update :game/events pop)
        (assoc :game/battlemap (l/set-state (:game/battlefield state) :default))
        (ce/event-transition event)
        (trigger-event event))
    (-> (ce/pop-phase state)
        (dissoc :game/battlemap))))


(defmethod trigger-event :dangerous
  [state event]
  (let [{:keys [unit/player entity/name unit/id]} event]
    (if-let [cube (get-in state [:game/units player name :cubes id])]
      (let [unit (get-in state [:game/battlefield cube])
            models (mu/models unit)
            roll (cd/roll! models)
            models-destroyed (cd/matches roll 1)
            unit-destroyed? (<= models models-destroyed)]
        (-> (if unit-destroyed?
              (cu/destroy-unit state unit)
              (cu/destroy-models state unit models-destroyed))
            (update :game/trigger assoc
                    :unit unit
                    :models-destroyed models-destroyed
                    :unit-destroyed? unit-destroyed?
                    :roll roll)
            (cb/refresh-battlemap [cube])
            (update :game/battlemap l/set-state [cube] :marked)))
      (trigger state))))


(defn to-charge
  [{:keys [game/player] :as state}]
  (let [unit-cubes (cu/unit-cubes state)
        player-cubes (cu/unit-cubes state player)
        charger-cubes (filter #(lm/charger? (:game/battlefield state) %) player-cubes)]
    (-> (assoc state
               :game/phase :charge
               :game/subphase :select-hex)
        (update :game/battlefield lu/phase-reset unit-cubes)
        (update :game/battlefield l/set-state :default)
        (update :game/battlefield l/set-state charger-cubes :selectable))))


(defmethod unselect :charge
  [state]
  (-> (assoc state :game/subphase :select-hex)
      (dissoc :game/selected
              :game/battlemap)))


(defmethod select [:charge :select-hex]
  [state cube]
  (if (or (= cube (:game/selected state))
          (= cube (get-in state [:game/charge :pointer :cube])))
    (unselect state)
    (let [{:keys [battlemap breadcrumbs pointer->events ranges]} (lm/show-charge (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/subphase :select-target
                 :game/selected cube
                 :game/battlemap battlemap)
          (update :game/charge assoc
                  :battlemap battlemap
                  :breadcrumbs breadcrumbs
                  :pointer->events pointer->events
                  :ranges ranges)))))


(defmethod select [:charge :select-target]
  [state cube]
  (-> (assoc state :game/subphase :select-hex)
      (select cube)))


(defmethod select [:charge :declare]
  [state cube]
  (-> (assoc state :game/subphase :select-hex)
      (select cube)))


(defn to-movement
  [{:keys [game/player game/battlefield] :as state}]
  (let [player-cubes (cu/unit-cubes state player)
        movable-cubes (remove #(lu/battlefield-engaged? battlefield %) player-cubes)]
    (-> (assoc state
               :game/phase :movement
               :game/subphase :select-hex)
        (update :game/battlefield l/set-state :default)
        (update :game/battlefield l/set-state movable-cubes :selectable))))


(defmethod select [:movement :select-hex]
  [state cube]
  (-> (assoc state :game/subphase :reform)
      (select cube)))


(defmethod unselect :movement
  [state]
  (-> (assoc state :game/subphase :select-hex)
      (dissoc :game/selected
              :game/battlemap
              :game/movement)))


(defn skip-movement
  [state]
  (let [cube (:game/selected state)]
    (-> (assoc-in state [:game/battlefield cube :entity/state] :default)
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
  (let [unit (get-in state [:game/battlefield (:cube pointer)])]
    (-> (if (= (:facing pointer) (:unit/facing unit))
          (dissoc state :game/movement)
          (assoc state :game/movement {:moved? true}))
        (update :game/battlemap cm/set-mover-selected pointer)
        (assoc-in [:game/movement :pointer] pointer))))


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
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        pointer (get-in state [:game/movement :pointer])
        events (get-in state [:game/movement :pointer->events pointer])
        old-terrain (lt/pickup unit)
        new-terrain (get-in state [:game/battlefield (:cube pointer)])
        updated-unit (-> (assoc unit
                                :entity/state :default
                                :entity/cube (:cube pointer)
                                :unit/facing (:facing pointer))
                         (lt/swap new-terrain))]
    (-> (assoc-in state [:game/battlefield cube] old-terrain)
        (assoc-in [:game/battlefield (:cube pointer)] updated-unit)
        (assoc-in [:game/units (:unit/player unit) (:entity/name unit) :cubes (:unit/id unit)]
                  (:cube pointer))
        (update :game/events into events)
        (unselect)
        (trigger))))


(defn movement-transition
  [state movement]
  (-> (assoc state :game/subphase movement)
      (dissoc :game/movement)
      (select (:game/selected state))))


(defmethod select [:movement :reform]
  [state cube]
  (if (= cube (get-in state [:game/movement :pointer :cube]))
    (unselect state)
    (let [unit (get-in state [:game/battlefield cube])
          pointer (mc/->Pointer cube (:unit/facing unit))
          battlemap (lm/show-reform (:game/battlefield state) cube)]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap)
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
