(ns hexenhammer.render.entity
  (:require [hexenhammer.render.svg :as rs]))


(defmulti render (fn [entity cube] (:entity/class entity)))


(defmethod render :terrain
  [terrain cube]
  (let [type-str (name (:terrain/type terrain))]
    (-> (rs/hexagon)
        (rs/add-classes ["terrain" type-str])
        (rs/translate cube))))
