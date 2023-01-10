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
  (int/svg-hexagon 1 2 3 4 :colour "green")
  => [:svg-hexagon 1 2 3 4 "green"]

  (int/svg-coordinates 1 2 3 4)
  => [:svg-coordinates 1 2 3 4]))
