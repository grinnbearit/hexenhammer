(ns hexenhammer.model.logic.entity)


(defn unit?
  [entity]
  (= :unit (:entity/class entity)))


(defn terrain?
  [entity]
  (= :terrain (:entity/class entity)))
