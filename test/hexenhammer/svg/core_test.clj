(ns hexenhammer.svg.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.svg.core :refer :all]
            [hexenhammer.svg.internal :as int]
            [hexenhammer.cube :as cube]))


(facts
 "svg unit"

 (svg-unit 0 (cube/->Cube 0 0 0) {:unit/name "unit-name"
                                  :unit/id "i"
                                  :unit/models 12
                                  :unit/facing :s})
 => [:g {:transform "translate(40.00, 34.50)"}
     [:hexagon :fill "#8b0000"]
     [:text -1 "unitname"]
     [:text 0 "i"]
     [:text 1 "(12)"]
     [:chevron :s]]

 (provided
  (int/svg-hexagon :fill "#8b0000") => [:hexagon :fill "#8b0000"]
  (int/svg-text -1 "unit-name") => [:text -1 "unitname"]
  (int/svg-text 0 "i") => [:text 0 "i"]
  (int/svg-text 1 "(12)") => [:text 1 "(12)"]
  (int/svg-chevron :s) => [:chevron :s]))


(facts
 "svg grass"

 (let [cube (cube/->Cube 0 0 0)]

   (svg-grass 0 cube)
   => [:g {:transform "translate(40.00, 34.50)"}
       [:hexagon :fill "green"]
       [:svg-coordinates 0 0 0]]

   (provided
    (int/svg-hexagon :fill "green") => [:hexagon :fill "green"]
    (int/svg-coordinates cube) => [:svg-coordinates 0 0 0])))


(facts
 "state -> svg"


 (state->svg {:map/size 0 :map/units {}})
 => [[:svg-grass 0 0 0 0]]

 (provided
  (svg-grass 0 (cube/->Cube 0 0 0)) => [:svg-grass 0 0 0 0])


 (state->svg {:map/size 1 :map/units {(cube/->Cube 0 0 0) :unit}})
 => [[:svg-grass 1 -1 0 1]
     [:svg-grass 1 -1 1 0]
     [:svg-grass 1 0 -1 1]
     [:svg-unit 1 0 0 0 :unit]
     [:svg-grass 1 0 1 -1]
     [:svg-grass 1 1 -1 0]
     [:svg-grass 1 1 0 -1]]

 (provided
  (svg-grass 1 (cube/->Cube -1 0 1)) => [:svg-grass 1 -1 0 1]
  (svg-grass 1 (cube/->Cube -1 1 0)) => [:svg-grass 1 -1 1 0]
  (svg-grass 1 (cube/->Cube 0 -1 1)) => [:svg-grass 1 0 -1 1]
  (svg-grass 1 (cube/->Cube 0 1 -1)) => [:svg-grass 1 0 1 -1]
  (svg-grass 1 (cube/->Cube 1 -1 0)) => [:svg-grass 1 1 -1 0]
  (svg-grass 1 (cube/->Cube 1 0 -1)) => [:svg-grass 1 1 0 -1]
  (svg-unit 1 (cube/->Cube 0 0 0) :unit) => [:svg-unit 1 0 0 0 :unit]))


(facts
 "render state"

 (render-state {:map/size 0})
 => [:html
     [:head]
     [:body
      [:svg {:width 200 :height 100}
       [:state->svg {:map/size 0}]]]]

 (provided
  (int/size->dim 0) => {:width 200 :height 100}
  (state->svg {:map/size 0}) => [:state->svg {:map/size 0}]))
