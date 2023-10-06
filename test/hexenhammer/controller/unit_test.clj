(ns hexenhammer.controller.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.event :as mv]
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
                 :cube-1)
   => {:game/units {1 {"unit" {:cubes {}}}}
       :game/battlefield {:cube-1 :terrain}}

   (provided
    (lt/pickup unit) => :terrain)))


(facts
 "damage unit"

 (let [state {:game/battlefield {:cube-2 :unit-1}}]

   (damage-unit state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (lu/damage-unit :unit-1 2) => :unit-2
    (lu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false))


 (let [unit {:entity/cube :cube-2
             :unit/player 1
             :entity/name "unit"
             :unit/id 2}

       state {:game/battlefield {:cube-2 unit}
              :game/events []}]

   (damage-unit state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}
       :game/events [:heavy-casualties]}

   (provided
    (lu/damage-unit unit 2) => :unit-2
    (lu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => true
    (mv/heavy-casualties :cube-1 1 "unit" 2) => :heavy-casualties)))


(facts
 "destroy models"

 (let [state {:game/battlefield {:cube-2 :unit-1}}]

   (destroy-models state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (lu/destroy-models :unit-1 2) => :unit-2
    (lu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false))


 (let [unit {:entity/cube :cube-2
             :unit/player 1
             :entity/name "unit"
             :unit/id 2}

       state {:game/battlefield {:cube-2 unit}
              :game/events []}]

   (destroy-models state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}
       :game/events [:heavy-casualties]}

   (provided
    (lu/destroy-models unit 2) => :unit-2
    (lu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => true
    (mv/heavy-casualties :cube-1 1 "unit" 2) => :heavy-casualties)))


(facts
 "move unit"
 (let [pointer (mc/->Pointer :cube-2 :n)
       battlefield {:cube-1 {:unit/player 1
                             :entity/name "unit"
                             :unit/id 2}}]

   (move-unit {:units {1 {"unit" {2 :cube-1}}}
               :game/battlefield battlefield}
              :cube-1
              pointer)
   => {:units {1 {"unit" {2 :cube-2}}}
       :game/battlefield :battlefield-2}

   (provided
    (lu/move-unit battlefield :cube-1 pointer)
    => :battlefield-2)))
