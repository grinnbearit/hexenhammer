(ns hexenhammer.svg.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.svg.core :refer :all]
            [hexenhammer.svg.internal :as int]
            [hexenhammer.svg.render :as render]
            [hexenhammer.cube :as cube]))


(facts
 "state -> svg"

 (let [cube-0 (cube/->Cube 0 0 0)
       cube-1 (cube/->Cube 1 0 -1)]

   (state->svg {:map/battlefield {cube-0 {:hexenhammer/class :terrain}
                                  cube-1 {:hexenhammer/class :unit}}})
   => [[:svg-terrain {:transform "translate(40.00, 34.50)"}]
       [:svg-unit {:transform "translate(100.00, 69.00)"}]]

   (provided
    (render/svg-terrain cube-0) => [:svg-terrain {}]
    (render/svg-unit {:hexenhammer/class :unit}) => [:svg-unit {}])))
