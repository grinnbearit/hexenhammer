(ns hexenhammer.controller.core
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.core :as lc]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.movement :as lm]
            [hexenhammer.controller.battlefield :as cb]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.dice :as cd]))


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
        id (inc (get-in state [:game/units player :counter]))
        unit (-> (me/gen-unit cube player id facing :M M :Ld Ld)
                 (assoc :entity/state :selectable)
                 (lt/place terrain))]
    (-> (assoc-in state [:game/battlefield cube] unit)
        (assoc-in [:game/units player :cubes id] cube)
        (assoc-in [:game/units player :counter] id)
        (unselect))))


(defn remove-unit
  [state]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        terrain (-> (lt/pickup unit)
                    (assoc :entity/state :selectable))]
    (-> (assoc-in state [:game/battlefield cube] terrain)
        (update-in [:game/units (:unit/player unit) :cubes] dissoc (:unit/id unit))
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
        (unselect)
        (select cube))))


(defn to-charge
  [state]
  (-> (assoc state
             :game/phase :charge
             :game/subphase :select-hex)
      (update :game/battlefield cb/set-state :default)))


(defn to-movement
  [{:keys [game/player game/battlefield] :as state}]
  (let [player-cubes (vals (get-in state [:game/units player :cubes]))
        movable-cubes (remove #(lc/battlefield-engaged? battlefield %) player-cubes)]
    (-> (assoc state
               :game/phase :movement
               :game/subphase :select-hex)
        (update :game/battlefield cb/set-state :default)
        (update :game/battlefield cb/set-state movable-cubes :selectable))))


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
        old-terrain (lt/pickup unit)
        new-terrain (get-in state [:game/battlefield (:cube pointer)])
        updated-unit (-> (assoc unit
                                :entity/state :default
                                :entity/cube (:cube pointer)
                                :unit/facing (:facing pointer))
                         (lt/swap new-terrain))]
    (-> (assoc-in state [:game/battlefield cube] old-terrain)
        (assoc-in [:game/battlefield (:cube pointer)] updated-unit)
        (assoc-in [:game/units (:unit/player unit) :cubes (:unit/id unit)] (:cube pointer))
        (unselect))))


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
    (let [{:keys [battlemap path-map]}  (lm/show-forward (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap)
          (update :game/movement assoc
                  :battlemap battlemap
                  :path-map path-map)
          (move pointer)))))


(defmethod select [:movement :reposition]
  [state cube]
  (if (= cube (get-in state [:game/movement :pointer :cube]))
    (unselect state)
    (let [{:keys [battlemap path-map]}  (lm/show-reposition (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap)
          (update :game/movement assoc
                  :battlemap battlemap
                  :path-map path-map)
          (move pointer)))))


(defmethod select [:movement :march]
  [state cube]
  (if (= cube (get-in state [:game/movement :pointer :cube]))
    (unselect state)
    (let [{:keys [battlemap path-map]}  (lm/show-march (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))
          threats (lm/show-threats (:game/battlefield state) cube)
          threat-map (merge battlemap threats)
          unit (get-in state [:game/battlefield cube])]

      (-> (assoc-in state [:game/movement :march]
                    (if (seq threats)
                      (if (get-in unit [:unit/movement :marched?])
                        (if (get-in unit [:unit/movement :passed?]) :passed :failed)
                        :required)
                      :unnecessary))

          (assoc :game/selected cube
                 :game/battlemap threat-map)
          (update :game/movement assoc
                  :battlemap threat-map
                  :path-map path-map)

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
