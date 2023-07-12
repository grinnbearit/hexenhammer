(ns hexenhammer.controller.core
  (:require [hexenhammer.model.entity :as entity]))


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
  (let [unit (entity/gen-unit (:game/selected state) player facing :interaction :selectable)]
    (-> (assoc-in state [:game/battlefield (:game/selected state)] unit)
        (unselect))))


(defn remove-unit
  [state]
  (let [terrain (entity/gen-terrain (:game/selected state) :interaction :selectable)]
    (-> (assoc-in state [:game/battlefield (:game/selected state)] terrain)
        (unselect))))
