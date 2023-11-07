(ns hexenhammer.logic.battlefield.unit
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :refer :all]))


(facts
 "remove unit"

 (let [battlefield {:cube-1 :unit-1
                    :cube-2 :terrain-2}]

   (remove-unit battlefield :cube-1)
   => {:cube-1 :terrain-1
       :cube-2 :terrain-2}

   (provided
    (let/clear :unit-1) => :terrain-1)))



(facts
 "engaged?"

 (let [battlefield {:cube-1 :unit-1
                    :cube-2 :unit-2}]

   (engaged? battlefield :cube-1 :cube-2)
   => false

   (provided
    (leu/enemies? :unit-1 :unit-2) => false))


 (let [unit-1 {:unit/id 1 :unit/facing :n}
       unit-2 {:unit/id 2 :unit/facing :n}
       battlefield {:cube-1 unit-1
                    :cube-2 unit-2}]

   (engaged? battlefield :cube-1 :cube-2)
   => false

   (provided
    (leu/enemies? unit-1 unit-2) => true

    (lc/forward-arc :cube-1 :n) => [:cube-3 :cube-4]
    (lc/forward-arc :cube-2 :n) => [:cube-5 :cube-6])


   (engaged? battlefield :cube-1 :cube-2)
   => true

   (provided
    (leu/enemies? unit-1 unit-2) => true
    (lc/forward-arc :cube-1 :n) => [:cube-2])


   (engaged? battlefield :cube-1 :cube-2)
   => true

   (provided
    (leu/enemies? unit-1 unit-2) => true

    (lc/forward-arc :cube-1 :n) => [:cube-3 :cube-4]
    (lc/forward-arc :cube-2 :n) => [:cube-1]))


 (engaged? :battlefield :cube-1)
 => false

 (provided
  (lc/neighbours :cube-1) => [])


 (let [battlefield {:cube-1 :unit-1}]

   (engaged? battlefield :cube-1)
   => false

   (provided
    (lc/neighbours :cube-1) => [:cube-2]))


 (let [battlefield {:cube-1 :unit-1
                    :cube-2 :terrain-1}]

   (engaged? battlefield :cube-1)
   => false

   (provided
    (lc/neighbours :cube-1) => [:cube-2]

    (leu/unit? :terrain-1) => false))


 (let [battlefield {:cube-1 :unit-1
                    :cube-2 :unit-2}]

   (engaged? battlefield :cube-1)
   => false

   (provided
    (lc/neighbours :cube-1) => [:cube-2]

    (leu/unit? :unit-2) => true

    (leu/enemies? :unit-1 :unit-2) => false))


 (let [unit-1 {:unit/id 1 :unit/facing :n}
       unit-2 {:unit/id 2 :unit/facing :n}
       battlefield {:cube-1 unit-1
                    :cube-2 unit-2}]

   (engaged? battlefield :cube-1)
   => true

   (provided
    (lc/neighbours :cube-1) => [:cube-2]

    (leu/unit? unit-2) => true

    (leu/enemies? unit-1 unit-2) => true
    (lc/forward-arc :cube-1 :n) => [:cube-2])))


(facts
 "movable?"

 (let [battlefield {:cube-1 :unit-1}]

   (movable? battlefield :cube-1)
   => false

   (provided
    (engaged? battlefield :cube-1) => true)


   (movable? battlefield :cube-1)
   => false

   (provided
    (engaged? battlefield :cube-1) => false
    (leu/fleeing? :unit-1) => true)


   (movable? battlefield :cube-1)
   => true

   (provided
    (engaged? battlefield :cube-1) => false
    (leu/fleeing? :unit-1) => false)))


(facts
 "unit key"

 (let [battlefield {:cube-1 :unit-1}]

   (unit-key battlefield :cube-1)
   => :unit-key-1

   (provided
    (leu/unit-key :unit-1) => :unit-key-1)))


(facts
 "move unit"

 (let [battlefield {:cube-1 {:entity/class :unit
                             :unit/facing :n}
                    :cube-2 :terrain-2}]

   (move-unit battlefield :cube-1 (lc/->Pointer :cube-2 :s))
   => {:cube-1 :terrain-1
       :cube-2 :unit-2}

   (provided
    (remove-unit battlefield :cube-1)
    => {:cube-1 :terrain-1
        :cube-2 :terrain-2}

    (let/place :terrain-2 {:entity/class :unit
                           :unit/facing :s})
    => :unit-2)))


(facts
 "unit pointer"

 (let [battlefield {:cube-1 {:unit/facing :n}}]

   (unit-pointer battlefield :cube-1) => (lc/->Pointer :cube-1 :n)))


(facts
 "reset phase"

 (let [battlefield {:cube-1 :unit-1}]

   (reset-phase battlefield :cube-1)
   => {:cube-1 :unit-2}

   (provided
    (leu/reset-phase :unit-1) => :unit-2)))


(facts
 "panickable?"

 (let [battlefield {:cube-1 :unit-1}]

   (panickable? battlefield :cube-1)
   => false

   (provided
    (leu/panicked? :unit-1) => true))


 (let [battlefield {:cube-1 :unit-1}]

   (panickable? battlefield :cube-1)
   => false

   (provided
    (leu/panicked? :unit-1) => false
    (leu/fleeing? :unit-1) => true))


 (let [battlefield {:cube-1 :unit-1}]

   (panickable? battlefield :cube-1)
   => false

   (provided
    (leu/panicked? :unit-1) => false
    (leu/fleeing? :unit-1) => false
    (engaged? battlefield :cube-1) => true))


 (let [battlefield {:cube-1 :unit-1}]

   (panickable? battlefield :cube-1)
   => true

   (provided
    (leu/panicked? :unit-1) => false
    (leu/fleeing? :unit-1) => false
    (engaged? battlefield :cube-1) => false)))


(facts
 "heavy casualties?"

 (let [battlefield {:cube-1 :unit-1}]

   (heavy-casualties? battlefield :cube-1)
   => false

   (provided
    (leu/unit-strength :unit-1) => 12
    (leu/phase-strength :unit-1) => 12))


 (let [battlefield {:cube-1 :unit-1}]

   (heavy-casualties? battlefield :cube-1)
   => false

   (provided
    (leu/unit-strength :unit-1) => 7
    (leu/phase-strength :unit-1) => 12
    (panickable? battlefield :cube-1) => false))


 (let [battlefield {:cube-1 :unit-1}]

   (heavy-casualties? battlefield :cube-1)
   => true

   (provided
    (leu/unit-strength :unit-1) => 7
    (leu/phase-strength :unit-1) => 12
    (panickable? battlefield :cube-1) => true)))
