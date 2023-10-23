(ns hexenhammer.render.entity
  (:require [hexenhammer.render.svg :as rs]))


(defn render-base
  "Generates a base for an entity"
  [terrain]
  (let [type-str (name (:terrain/type terrain))
        presentation-str (name (:entity/presentation terrain))]
    (-> (rs/hexagon)
        (rs/add-classes ["terrain" type-str presentation-str]))))


(defmulti render (fn [entity phase cube] (:entity/class entity)))


(defmethod render :terrain
  [terrain phase cube]
  (-> (render-base terrain)
      (rs/translate cube)
      (rs/if-selectable (:entity/presentation terrain) phase cube)))
