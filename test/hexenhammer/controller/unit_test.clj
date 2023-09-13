(ns hexenhammer.controller.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.unit :as lu]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.controller.unit :refer :all]))


(facts
 "unit cubes"

 (unit-cubes {:game/units {1 {"unit-1" {:cubes {1 :cube-1
                                                2 :cube-2}}
                              "unit-2" {:cubes {1 :cube-3}}}}}
             1)
 => [:cube-1 :cube-2 :cube-3]


 (unit-cubes {:game/units {1 {"unit-1" {:cubes {1 :cube-1
                                                2 :cube-2}}
                              "unit-2" {:cubes {1 :cube-3}}}
                           2 {"unit-1" {:cubes {1 :cube-4}}}}})
 => [:cube-1 :cube-2 :cube-3 :cube-4])


(facts
 "destroy unit"

 (let [unit {:entity/cube :cube-1
             :entity/name "unit"
             :unit/player 1
             :unit/id 2}]

   (destroy-unit {:game/units {1 {"unit" {:cubes {2 :cube-1}}}}
                  :game/battlefield {:cube-1 unit}}
                 unit)
   => {:game/units {1 {"unit" {:cubes {}}}}
       :game/battlefield {:cube-1 :terrain}}

   (provided
    (lt/pickup unit) => :terrain)))


(facts
 "damage unit"

 (let [unit {:entity/cube :cube-1}]

   (damage-unit {:game/battlefield {:cube-1 unit}} unit 2)
   => {:game/battlefield {:cube-1 :unit-2}}

   (provided
    (lu/damage-unit unit 2) => :unit-2)))


(facts
 "destroy models"

 (let [unit {:entity/cube :cube-1}]

   (destroy-models {:game/battlefield {:cube-1 unit}} unit 2)
   => {:game/battlefield {:cube-1 :unit-2}}

   (provided
    (lu/destroy-models unit 2) => :unit-2)))
