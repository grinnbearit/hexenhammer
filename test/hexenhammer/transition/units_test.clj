(ns hexenhammer.transition.units-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.units :refer :all]))


(facts
 "unit cubes"

 (let [units {1 {"unit-1" {:cubes {1 :cube-1
                                   2 :cube-2}}
                 "unit-2" {:cubes {1 :cube-3}}}
              2 {"unit-1" {:cubes {1 :cube-4}}}}]

   (unit-cubes units 1)
   => [:cube-1 :cube-2 :cube-3]


   (unit-cubes units)
   => [:cube-1 :cube-2 :cube-3 :cube-4]))


(facts
 "next id"

 (next-id {} 1 "unit") => 1

 (next-id {1 {"unit" {:counter 1}}} 1 "unit") => 2)


(facts
 "inc id"

 (inc-id {} 1 "unit")
 => {1 {"unit" {:counter 1}}}

 (inc-id {1 {"unit" {:counter 1}}} 1 "unit")
 => {1 {"unit" {:counter 2}}})


(facts
 "set unit"

 (set-unit {} {:unit/player 1 :unit/name "unit" :unit/id 2} :cube-1)
 => {1 {"unit" {:cubes {2 :cube-1}}}})


(facts
 "get unit"

 (get-unit {} {:unit/player 1 :unit/name "unit" :unit/id 2}) => nil

 (get-unit {1 {"unit" {:cubes {2 :cube-1}}}}
           {:unit/player 1 :unit/name "unit" :unit/id 2})
 => :cube-1)


(facts
 "remove unit"

 (remove-unit {1 {"unit" {:cubes {2 :cube-1}}}}
              {:unit/player 1 :unit/name "unit" :unit/id 2})
 => {1 {"unit" {:cubes {}}}})
