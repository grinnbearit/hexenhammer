(ns hexenhammer.svg.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.svg.core :refer :all]
            [hexenhammer.svg.internal :as int]))


(facts
 "svg unit"

 (svg-unit 0 0 0 0 {:unit/name "unit-name"
                    :unit/id 1
                    :unit/models 12})
 => [:g {:transform "translate(60.00, 52.00)"}
     [:hexagon :fill "#8b0000"]
     [:text -1 "unitname - 1"]
     [:text 1 "(12)"]]

 (provided
  (int/svg-hexagon :fill "#8b0000") => [:hexagon :fill "#8b0000"]
  (int/svg-text -1 "unit-name - 1") => [:text -1 "unitname - 1"]
  (int/svg-text 1 "(12)") => [:text 1 "(12)"]))


(facts
 "svg grass"

 (svg-grass 0 0 0 0)
 => [:g {:transform "translate(60.00, 52.00)"}
     [:hexagon :fill "green"]
     [:svg-coordinates 0 0 0]]

 (provided
  (int/svg-hexagon :fill "green") => [:hexagon :fill "green"]
  (int/svg-coordinates 0 0 0) => [:svg-coordinates 0 0 0]))


(facts
 "state -> svg"


 (state->svg {:map/size 0 :map/units {}})
 => [[:svg-grass 0 0 0 0]]

 (provided
  (svg-grass 0 0 0 0) => [:svg-grass 0 0 0 0])


 (state->svg {:map/size 1 :map/units {[0 0 0] :unit}})
 => [[:svg-grass 1 -1 0 1]
     [:svg-grass 1 -1 1 0]
     [:svg-grass 1 0 -1 1]
     [:svg-unit 1 0 0 0 :unit]
     [:svg-grass 1 0 1 -1]
     [:svg-grass 1 1 -1 0]
     [:svg-grass 1 1 0 -1]]

 (provided
  (svg-grass 1 -1 0 1) => [:svg-grass 1 -1 0 1]
  (svg-grass 1 -1 1 0) => [:svg-grass 1 -1 1 0]
  (svg-grass 1 0 -1 1) => [:svg-grass 1 0 -1 1]
  (svg-grass 1 0 1 -1) => [:svg-grass 1 0 1 -1]
  (svg-grass 1 1 -1 0) => [:svg-grass 1 1 -1 0]
  (svg-grass 1 1 0 -1) => [:svg-grass 1 1 0 -1]
  (svg-unit 1 0 0 0 :unit) => [:svg-unit 1 0 0 0 :unit]))


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
