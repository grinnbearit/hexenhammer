(ns hexenhammer.transition.state.units-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.battlefield.unit :as lbu]
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
 "destroy unit"

 (let [state {:game/units :units-1
              :game/battlefield :battlefield-1}]

   (destroy-unit state :cube-1)
   => {:game/units :units-2
       :game/battlefield :battlefield-2}

   (provided
    (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1
    (tu/remove-unit :units-1 :unit-key-1) => :units-2
    (lbu/remove-unit :battlefield-1 :cube-1) => :battlefield-2)))


(facts
 "destroy models"

 (let [state {:game/battlefield {:cube-2 :unit-1}}]

   (destroy-models state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (leu/destroy-models :unit-1 2) => :unit-2
    (lbu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false))


 (let [state {:game/battlefield {:cube-2 :unit-1}
              :game/events []}]

   (destroy-models state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}
       :game/events [:heavy-casualties]}

   (provided
    (leu/destroy-models :unit-1 2) => :unit-2
    (lbu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => true
    (leu/unit-key :unit-1) => :unit-key-1
    (lev/heavy-casualties :cube-1 :unit-key-1) => :heavy-casualties)))


(facts
 "damage unit"

 (let [state {:game/battlefield {:cube-2 :unit-1}}]

   (damage-unit state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}}

   (provided
    (leu/damage-unit :unit-1 2) => :unit-2
    (lbu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => false))


 (let [state {:game/battlefield {:cube-2 :unit-1}
              :game/events []}]

   (damage-unit state :cube-2 :cube-1 2)
   => {:game/battlefield {:cube-2 :unit-2}
       :game/events [:heavy-casualties]}

   (provided
    (leu/damage-unit :unit-1 2) => :unit-2
    (lbu/heavy-casualties? {:cube-2 :unit-2} :cube-2) => true
    (leu/unit-key :unit-1) => :unit-key-1
    (lev/heavy-casualties :cube-1 :unit-key-1) => :heavy-casualties)))


(facts
 "escape unit"

 (let [state {:game/units :units-1
              :game/battlefield :battlefield-1}]

   (escape-unit state :cube-1 :cube-2)
   => {:game/units :units-2
       :game/battlefield :battlefield-2}

   (provided
    (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1
    (tu/remove-unit :units-1 :unit-key-1) => :units-2
    (lbu/remove-unit :battlefield-1 :cube-1) => :battlefield-2)))
