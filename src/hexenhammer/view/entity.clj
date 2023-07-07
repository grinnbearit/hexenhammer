(ns hexenhammer.view.entity
  (:require [hexenhammer.view.svg :as svg]))


(defmulti render :hexenhammer/entity)


(defmethod render :terrain
  [terrain]
  (svg/translate
   (:terrain/cube terrain)
   (svg/hexagon :classes ["terrain"])))
