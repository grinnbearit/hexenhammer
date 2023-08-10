(ns hexenhammer.controller.core
  (:require [hexenhammer.model.entity :as me]
            [hexenhammer.model.logic :as ml]
            [hexenhammer.controller.entity :as ce]
            [hexenhammer.controller.battlefield :as cb]))


(defmulti select (fn [state cube] [(:game/phase state) (:game/subphase state)]))


(defmethod select [:setup :select-hex]
  [state cube]
  (let [entity-class (get-in state [:game/battlefield cube :entity/class])
        to-subphase ({:terrain :add-unit :unit :remove-unit} entity-class)]

    (-> (assoc state :game/subphase to-subphase)
        (assoc :game/selected cube)
        (assoc-in [:game/battlefield cube :entity/presentation] :selected))))


(defn unselect
  "Reset to the default selection state"
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
  [state player facing]
  (let [cube (:game/selected state)
        id (inc (get-in state [:game/units player :counter]))
        unit (me/gen-unit cube player id facing :interaction :selectable)]
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
        movable-cubes (remove #(ml/battlefield-engaged? battlefield %) player-cubes)]
    (-> (assoc state
               :game/phase :movement
               :game/subphase :select-hex)
        (update :game/battlefield cb/reset-default)
        (update :game/battlefield cb/set-interactable movable-cubes))))


(defmethod select [:movement :select-hex]
  [state cube]
  (-> (assoc state :game/subphase :reform)
      (select cube)))


(defmethod select [:movement :reform]
  [state cube]
  (let [unit (get-in state [:game/battlefield cube])
        mover (me/gen-mover cube (:game/player state) (:unit/facing unit)
                            :presentation :selected)]
    (-> (assoc state :game/selected cube)
        (assoc :game/battlemap {cube mover})
        (dissoc :game/movement?))))


(defn skip-movement
  [state]
  (-> (update-in state [:game/battlefield (:game/selected state)] ce/reset-default)
      (dissoc :game/selected :game/battlemap)
      (assoc :game/subphase :select-hex)))


(defmulti move (fn [state cube facing] [(:game/phase state) (:game/subphase state)]))


(defmethod move [:movement :reform]
  [state cube facing]
  (let [unit (get-in state [:game/battlefield cube])]
    (-> (if (not= facing (:unit/facing unit))
          (assoc state :game/movement? true)
          (dissoc state :game/movement?))
        (assoc-in [:game/battlemap cube :unit/facing] facing))))


(defn finish-movement
  [state]
  (let [cube (:game/selected state)
        unit (get-in state [:game/battlefield cube])
        new-facing (get-in state [:game/battlemap cube :unit/facing])]
    (-> (assoc-in state [:game/battlefield cube]
                  (-> (ce/reset-default unit)
                      (assoc :unit/facing new-facing)))
        (dissoc :game/selected :game/battlemap :game/movement?)
        (assoc :game/subphase :select-hex))))
