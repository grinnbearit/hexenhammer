(ns hexenhammer.controller.core
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.logic.movement :as mlm]
            [hexenhammer.controller.entity :as ce]
            [hexenhammer.controller.battlefield :as cb]
            [hexenhammer.controller.movement :as cm]))


(defmulti select (fn [state cube] [(:game/phase state) (:game/subphase state)]))


(defmethod select [:setup :select-hex]
  [state cube]
  (let [entity-class (get-in state [:game/battlefield cube :entity/class])
        to-subphase ({:terrain :add-unit :unit :remove-unit} entity-class)]

    (-> (assoc state :game/subphase to-subphase)
        (assoc :game/selected cube)
        (assoc-in [:game/battlefield cube :entity/presentation] :selected))))


(defmulti unselect (fn [state] (:game/phase state)))


(defmethod unselect :setup
  [state]
  (-> (assoc state :game/subphase :select-hex)
      (assoc-in [:game/battlefield (:game/selected state) :entity/presentation] :default)
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
  [state player facing & {:keys [M]}]
  (let [cube (:game/selected state)
        id (inc (get-in state [:game/units player :counter]))
        unit (me/gen-unit cube player id facing :interaction :selectable
                          :M M)]
    (-> (assoc-in state [:game/battlefield cube] unit)
        (assoc-in [:game/units player :cubes id] cube)
        (assoc-in [:game/units player :counter] id)
        (unselect))))


(defn remove-unit
  [state]
  (let [cube (:game/selected state)
        terrain (me/gen-terrain cube :interaction :selectable)
        unit (get-in state [:game/battlefield cube])]
    (-> (assoc-in state [:game/battlefield cube] terrain)
        (update-in [:game/units (:unit/player unit) :cubes] dissoc (:unit/id unit))
        (unselect))))


(defn to-movement
  [{:keys [game/player game/battlefield] :as state}]
  (let [player-cubes (vals (get-in state [:game/units player :cubes]))
        movable-cubes (remove #(mlc/battlefield-engaged? battlefield %) player-cubes)]
    (-> (assoc state
               :game/phase :movement
               :game/subphase :select-hex)
        (update :game/battlefield cb/reset-default)
        (update :game/battlefield cb/set-interactable movable-cubes))))


(defmethod select [:movement :select-hex]
  [state cube]
  (-> (assoc state :game/subphase :reform)
      (select cube)))


(defmethod unselect :movement
  [state]
  (-> (assoc state :game/subphase :select-hex)
      (dissoc :game/selected :movement/selected :game/battlemap)))


(defn skip-movement
  [state]
  (-> (update-in state [:game/battlefield (:game/selected state)] ce/reset-default)
      (dissoc :game/battlemap :game/selected)
      (assoc :game/subphase :select-hex)))


(defmulti move (fn [state pointer] [(:game/phase state) (:game/subphase state)]))


(defmethod move [:movement :reform]
  [state pointer]
  (let [unit (get-in state [:game/battlefield (:cube pointer)])]
    (-> (if (not= (:facing pointer) (:unit/facing unit))
          (assoc state :movement/moved? true)
          (dissoc state :movement/moved?))
        (update :game/battlemap cm/set-mover-selected pointer)
        (assoc :movement/selected pointer))))


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
        pointer (:movement/selected state)
        terrain (me/gen-terrain cube)
        updated-unit (-> (ce/reset-default unit)
                         (assoc :entity/cube (:cube pointer)
                                :unit/facing (:facing pointer)))]
    (-> (assoc-in state [:game/battlefield cube] terrain)
        (assoc-in [:game/battlefield (:cube pointer)] updated-unit)
        (assoc-in [:game/units (:unit/player unit) :cubes (:unit/id unit)] (:cube pointer))
        (dissoc :game/battlemap)
        (assoc :game/subphase :select-hex))))


(defn movement-transition
  [state movement]
  (-> (assoc state :game/subphase movement)
      (dissoc :movement/selected)
      (select (:game/selected state))))


(defmethod select [:movement :reform]
  [state cube]
  (if (= cube (get-in state [:movement/selected :cube]))
    (unselect state)
    (let [unit (get-in state [:game/battlefield cube])
          pointer (mc/->Pointer cube (:unit/facing unit))
          battlemap (mlm/show-reform (:game/battlefield state) cube)]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap)
          (move pointer)))))


(defmethod select [:movement :forward]
  [state cube]
  (if (= cube (get-in state [:movement/selected :cube]))
    (unselect state)
    (let [{:keys [battlemap breadcrumbs]}  (mlm/show-forward (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap
                 :movement/battlemap battlemap
                 :movement/breadcrumbs breadcrumbs)
          (move pointer)))))


(defmethod select [:movement :reposition]
  [state cube]
  (if (= cube (get-in state [:movement/selected :cube]))
    (unselect state)
    (let [{:keys [battlemap breadcrumbs]}  (mlm/show-reposition (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap battlemap
                 :movement/battlemap battlemap
                 :movement/breadcrumbs breadcrumbs)
          (move pointer)))))


(defmethod select [:movement :march]
  [state cube]
  (if (= cube (get-in state [:movement/selected :cube]))
    (unselect state)
    (let [{:keys [battlemap breadcrumbs]}  (mlm/show-march (:game/battlefield state) cube)
          pointer (mc/->Pointer cube (get-in state [:game/battlefield cube :unit/facing]))
          threats (mlm/show-threats (:game/battlefield state) cube)
          threat-map (merge battlemap threats)]
      (-> (assoc state
                 :game/selected cube
                 :game/battlemap threat-map
                 :movement/battlemap threat-map
                 :movement/breadcrumbs breadcrumbs)
          (move pointer)))))
