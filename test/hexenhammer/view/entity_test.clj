(ns hexenhammer.view.entity
  (:require [midje.sweet :refer :all]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :refer :all]))


(facts
 "render terrain"

 (render {:hexenhammer/entity :terrain
          :terrain/cube :cube-1})
 => [:hexagon "terrain" :cube-1]

 (provided
  (svg/hexagon :classes ["terrain"])
  => [:hexagon "terrain"]

  (svg/translate :cube-1 [:hexagon "terrain"])
  => [:hexagon "terrain" :cube-1]))
