(ns hexenhammer.view.entity
  (:require [hexenhammer.view.svg :as svg]))


(defmulti render :entity/class)


(defmethod render :terrain
  [terrain]
  (cond-> (-> (svg/hexagon)
              (svg/add-classes ["terrain"])
              (svg/translate (:entity/cube terrain)))

    (= :selected (:entity/presentation terrain))
    (svg/add-classes ["selected"])

    (= :selectable (:entity/interaction terrain))
    (svg/selectable (:entity/cube terrain))))
