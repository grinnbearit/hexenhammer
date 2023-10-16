(ns hexenhammer.controller.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.model.event :as mv]
            [hexenhammer.logic.unit :as lu]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.controller.unit :refer :all]))


(facts
 "key -> cube"

 (let [state {:game/units {1 {"unit" {:cubes {2 :cube-1}}}}}]
   (key->cube state {:unit/player 1 :entity/name "unit" :unit/id 2}) => :cube-1
   (key->cube state {:unit/player 1 :entity/name "unit" :unit/id 3}) => nil))


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
 "remove unit"

 (let [unit {:entity/cube :cube-1
             :entity/name "unit"
             :unit/player 1
             :unit/id 2}

       battlefield {:cube-1 unit}]

   (remove-unit {:game/units {1 {"unit" {:cubes {2 :cube-1}}}}
                 :game/battlefield battlefield}
                :cube-1)
   => {:game/units {1 {"unit" {:cubes {}}}}
       :game/battlefield {:cube-1 :terrain}}

   (provided
    (lu/remove-unit battlefield :cube-1) => {:cube-1 :terrain})))


(facts
 "destroy unit"

 (let [state {:game/battlefield {:cube-1 {:unit/phase {:strength 7}}}}]

   (destroy-unit state :cube-1)
   => state

   (provided
    (remove-unit state :cube-1)
    => state))


 (let [battlefield {:cube-1 {:unit/phase {:strength 8}
                             :unit/player 1}}
       state {:game/battlefield battlefield
              :game/events []}]

   (destroy-unit state :cube-1)
   => (assoc state
             :game/events [:event-1])

   (provided
    (remove-unit state :cube-1)
    => state

    (lu/nearby-friend-annihilated-events battlefield :cube-1 1)
    => [:event-1])))


(facts
 "escape unit"

 (let [state {:game/battlefield {:cube-1 {:unit/phase {:strength 7}}}}]

   (escape-unit state :cube-1 :cube-2)
   => state

   (provided
    (remove-unit state :cube-1)
    => state))


 (let [battlefield {:cube-1 {:unit/phase {:strength 8}
                             :unit/player 1}}
       state {:game/battlefield battlefield
              :game/events []}]

   (escape-unit state :cube-1 :cube-2)
   => (assoc state
             :game/events [:event-1])

   (provided
    (remove-unit state :cube-1)
    => state

    (lu/nearby-friend-annihilated-events battlefield :cube-2 1)
    => [:event-1])))


(facts
 "damage unit"

 (let [state {:game/battlefield {:cube-2 :unit-1}}]

   (damage-unit state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (lu/damage-unit :unit-1 2) => :unit-2
    (lu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false))


 (let [state {:game/battlefield {:cube-2 :unit-1}
              :game/events []}]

   (damage-unit state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}
       :game/events [:heavy-casualties]}

   (provided
    (lu/damage-unit :unit-1 2) => :unit-2
    (lu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => true
    (mu/unit-key :unit-1) => :unit-key-1
    (mv/heavy-casualties :cube-1 :unit-key-1) => :heavy-casualties)))


(facts
 "destroy models"

 (let [state {:game/battlefield {:cube-2 :unit-1}}]

   (destroy-models state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (lu/destroy-models :unit-1 2) => :unit-2
    (lu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false))


 (let [state {:game/battlefield {:cube-2 :unit-1}
              :game/events []}]

   (destroy-models state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}
       :game/events [:heavy-casualties]}

   (provided
    (lu/destroy-models :unit-1 2) => :unit-2
    (lu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => true
    (mu/unit-key :unit-1) => :unit-key-1
    (mv/heavy-casualties :cube-1 :unit-key-1) => :heavy-casualties)))


(facts
 "move unit"
 (let [pointer (mc/->Pointer :cube-2 :n)
       battlefield {:cube-1 {:unit/player 1
                             :entity/name "unit"
                             :unit/id 2}}]

   (move-unit {:game/units {1 {"unit" {:cubes {2 :cube-1}}}}
               :game/battlefield battlefield}
              :cube-1
              pointer)
   => {:game/units {1 {"unit" {:cubes {2 :cube-2}}}}
       :game/battlefield :battlefield-2}

   (provided
    (lu/move-unit battlefield :cube-1 pointer)
    => :battlefield-2)))
