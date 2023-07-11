(ns hexenhammer.controller.core
  (:require [hexenhammer.model.entity :as entity]))


(defmulti select (fn [state cube] [(:game/phase state) (:game/subphase state)]))


(defmethod select [:setup :select-hex]
  [state cube]
  (-> (assoc state :game/subphase :add-unit)
      (assoc :game/selected cube)
      (assoc-in [:game/battlefield cube :entity/presentation] :selected)))


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
