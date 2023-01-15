(ns hexenhammer.svg.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.svg.core :refer :all]
            [hexenhammer.svg.internal :as int]))


(facts
 "render grass"

 (render-grass 1 2 3 4)
 => [[:svg-hexagon 1 2 3 4 "green"]
     [:svg-coordinates 1 2 3 4]]

 (provided
  (int/svg-hexagon 1 2 3 4 :fill "green")
  => [:svg-hexagon 1 2 3 4 "green"]

  (int/svg-coordinates 1 2 3 4)
  => [:svg-coordinates 1 2 3 4]))


(facts
 "render unit"

 (render-unit 1 2 3 4 {:unit/name "warrior" :unit/id 1 :unit/models 12 :unit/facing :n})
 => [[:svg-hexagon 1 2 3 4 "#8b0000"]
     [:svg-unit 1 2 3 4 "warrior" 1 12]
     [:svg-facing 1 2 3 4 :n]]

 (provided
  (int/svg-hexagon 1 2 3 4 :fill "#8b0000")
  => [:svg-hexagon 1 2 3 4 "#8b0000"]

  (int/svg-unit 1 2 3 4 "warrior" 1 12)
  => [:svg-unit 1 2 3 4 "warrior" 1 12]

  (int/svg-facing 1 2 3 4 :n)
  => [:svg-facing 1 2 3 4 :n]))
