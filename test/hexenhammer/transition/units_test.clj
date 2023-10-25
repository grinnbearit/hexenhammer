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
