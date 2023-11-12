(ns hexenhammer.transition.state.units-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.event :as lbv]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.state.units :refer :all]))


(facts
 "move unit"

 (let [state {:game/units :units-1
              :game/battlefield :battlefield-1}
       pointer {:cube :cube-2 :facing :n}]

   (move-unit state :cube-1 pointer)
   => {:game/units :units-2
       :game/battlefield :battlefield-2}

   (provided
    (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1
    (tu/set-unit :units-1 :unit-key-1 :cube-2) => :units-2
    (lbu/move-unit :battlefield-1 :cube-1 pointer) => :battlefield-2)))


(facts
 "remove unit"

 (let [battlefield-1 {:cube-1 :unit-1}
       state {:game/player 1
              :game/units :units-1
              :game/battlefield battlefield-1}]

   (remove-unit state :cube-1 :cube-2)
   => {:game/player 1
       :game/units :units-2
       :game/battlefield :battlefield-2}

   (provided
    (leu/unit-key :unit-1) => :unit-key-1
    (lbu/remove-unit battlefield-1 :cube-1) => :battlefield-2
    (tu/remove-unit :units-1 :unit-key-1) => :units-2

    (leu/unit-strength :unit-1) => 7))


 (let [battlefield-1 {:cube-1 :unit-1}
       state {:game/player 1
              :game/units :units-1
              :game/battlefield battlefield-1
              :game/events []}]

   (remove-unit state :cube-1 :cube-2)
   => {:game/player 1
       :game/units :units-2
       :game/battlefield :battlefield-2
       :game/events [:event-1]}

   (provided
    (leu/unit-key :unit-1) => :unit-key-1
    (lbu/remove-unit battlefield-1 :cube-1) => :battlefield-2
    (tu/remove-unit :units-1 :unit-key-1) => :units-2

    (leu/unit-strength :unit-1) => 8
    (lbv/nearby-friend-annihilated :battlefield-2 :cube-2 1) => [:event-1])))


(facts
 "reduce unit"

 (let [state {:game/battlefield {:cube-2 :unit-1}}
       reducer-fn (constantly :unit-2)]

   (reduce-unit state :cube-2 :cube-1 reducer-fn)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (lbu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false))


 (let [state {:game/battlefield {:cube-2 :unit-1}
              :game/events []}
       reducer-fn (constantly :unit-2)]

   (reduce-unit state :cube-2 :cube-1 reducer-fn)
   => {:game/battlefield {:cube-2 :unit-2}
       :game/events [:heavy-casualties]}

   (provided
    (lbu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => true
    (leu/unit-key :unit-1) => :unit-key-1
    (lev/heavy-casualties :cube-1 :unit-key-1) => :heavy-casualties)))


(facts
 "destroy unit"

 (destroy-unit :state-1 :cube-1)
 => :remove-unit

 (provided
  (remove-unit :state-1 :cube-1 :cube-1) => :remove-unit))


(facts
 "escape unit"

 (escape-unit :state-1 :cube-1 :cube-2)
 => :remove-unit

 (provided
  (remove-unit :state-1 :cube-1 :cube-2) => :remove-unit))


(facts
 "destroy models"

 (let [state {:game/battlefield {:cube-2 :unit-1}}]

   (destroy-models state :cube-2 :cube-1 3)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (leu/destroy-models :unit-1 3) => :unit-2
    (lbu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false)))


(facts
 "damage unit"

 (let [state {:game/battlefield {:cube-2 :unit-1}}]

   (damage-unit state :cube-2 :cube-1 3)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (leu/damage-unit :unit-1 3) => :unit-2
    (lbu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false)))
