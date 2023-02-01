(ns hexenhammer.svg.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.svg.core :refer :all]
            [hexenhammer.svg.internal :as int]
            [hexenhammer.cube :as cube]))


(facts
 "svg unit"

 (svg-unit {:unit/name "unit-name"
            :unit/id "i"
            :unit/models 12
            :unit/facing :s})
 => [:g {}
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

   (svg-grass cube)
   => [:g {}
       [:hexagon :fill "green"]
       [:svg-coordinates 0 0 0]]

   (provided
    (int/svg-hexagon :fill "green") => [:hexagon :fill "green"]
    (int/svg-coordinates cube) => [:svg-coordinates 0 0 0])))


(facts
 "state -> svg"

 (let [cube-0 (cube/->Cube 0 0 0)
       cube-1 (cube/->Cube 1 0 -1)]

   (state->svg {:map/battlefield {cube-0 {:hexenhammer/class :terrain}
                                  cube-1 {:hexenhammer/class :unit}}})
   => [[:svg-grass {:transform "translate(40.00, 34.50)"}]
       [:svg-unit {:transform "translate(100.00, 69.00)"}]]

   (provided
    (svg-grass cube-0) => [:svg-grass {}]
    (svg-unit {:hexenhammer/class :unit}) => [:svg-unit {}])))


(facts
 "render state"

 (render-state {:map/rows 1 :map/columns 1})
 => [:html
     [:head]
     [:body
      [:svg {:width 200 :height 100}
       [:state->svg 1 1]]]]

 (provided
  (int/size->dim 1 1) => {:width 200 :height 100}
  (state->svg {:map/rows 1 :map/columns 1}) => [:state->svg 1 1]))
