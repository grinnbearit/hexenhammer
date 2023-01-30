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
 "gen battlefield cubes"

 (gen-battlefield-cubes 0 0)
 => []

 (gen-battlefield-cubes 1 2)
 => [(cube/->Cube 0 0 0)
     (cube/->Cube 1 0 -1)]

 (gen-battlefield-cubes 2 4)
 => [(cube/->Cube 0 0 0)
     (cube/->Cube 1 0 -1)
     (cube/->Cube 2 -1 -1)
     (cube/->Cube 3 -1 -2)

     (cube/->Cube 0 1 -1)
     (cube/->Cube 1 1 -2)
     (cube/->Cube 2 0 -2)
     (cube/->Cube 3 0 -3)])



(facts
 "state -> svg"

 (let [cube (cube/->Cube 0 0 0)]

   (state->svg {:map/rows 1 :map/columns 1 :map/units {}})
   => [[:svg-grass {:transform "translate(40.00, 34.50)"} cube]]

   (provided

    (gen-battlefield-cubes 1 1)
    => [cube]

    (svg-grass cube)
    => [:svg-grass {} cube])


   (state->svg {:map/rows 1 :map/columns 1 :map/units {cube :unit}})
   => [[:svg-unit {:transform "translate(40.00, 34.50)"} :unit]]

   (provided

    (gen-battlefield-cubes 1 1)
    => [cube]

    (svg-unit :unit)
    => [:svg-unit {} :unit])))


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
