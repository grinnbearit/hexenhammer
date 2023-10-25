(ns hexenhammer.logic.battlefield.unit
  (:require [midje.sweet :refer :all]
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
    (engaged? battlefield :cube-1) => true))


 (let [battlefield {:cube-1 {:unit/status {:fleeing? true}}}]

   (movable? battlefield :cube-1)
   => false

   (provided
    (engaged? battlefield :cube-1) => false))


 (let [battlefield {:cube-1 {}}]

   (movable? battlefield :cube-1)
   => true

   (provided
    (engaged? battlefield :cube-1) => false)))
