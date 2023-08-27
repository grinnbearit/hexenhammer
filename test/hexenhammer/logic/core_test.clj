(ns hexenhammer.logic.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.core :refer :all]))


(facts
 "enemies?"

 (enemies? {:unit/player 1}
           {:unit/player 1})
 => false

 (enemies? {:unit/player 1}
           {:unit/player 2})
 => true)


(facts
 "units engaged?"

 (engaged? :unit-1 :unit-2)
 => false

 (provided
  (enemies? :unit-1 :unit-2) => false)


 (engaged? {:unit/player 1
            :entity/cube :cube-1
            :unit/facing :n}
           {:unit/player 2
            :entity/cube :cube-2
            :unit/facing :n})
 => false

 (provided
  (mc/forward-arc :cube-1 :n)
  => [:cube-3 :cube-4]

  (mc/forward-arc :cube-2 :n)
  => [:cube-5 :cube-6])


 (engaged? {:unit/player 1
            :entity/cube :cube-1
            :unit/facing :n}
           {:unit/player 2
            :entity/cube :cube-2
            :unit/facing :n})
 => true

 (provided
  (mc/forward-arc :cube-1 :n)
  => [:cube-2])


 (engaged? {:unit/player 1
            :entity/cube :cube-1
            :unit/facing :n}
           {:unit/player 2
            :entity/cube :cube-2
            :unit/facing :n})
 => true

 (provided
  (mc/forward-arc :cube-1 :n)
  => [:cube-3 :cube-4]

  (mc/forward-arc :cube-2 :n)
  => [:cube-1]))


(facts
 "battlefield engaged?"

 (battlefield-engaged? {:cube-1 :unit-1} :cube-1)
 => false

 (provided
  (mc/neighbours :cube-1) => [])


 (battlefield-engaged? {:cube-1 :unit-1} :cube-1)
 => false

 (provided
  (mc/neighbours :cube-1) => [:cube-2])


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 :terrain-1}
                       :cube-1)
 => false

 (provided
  (mc/neighbours :cube-1) => [:cube-2]

  (le/unit? :terrain-1) => false)


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 :unit-2}
                       :cube-1)
 => false

 (provided
  (mc/neighbours :cube-1) => [:cube-2]

  (le/unit? :unit-2) => true

  (engaged? :unit-1 :unit-2) => false)


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 :unit-2}
                       :cube-1)
 => true

 (provided
  (mc/neighbours :cube-1) => [:cube-2]

  (le/unit? :unit-2) => true

  (engaged? :unit-1 :unit-2) => true))


(facts
 "battlefield visible?"

 (battlefield-visible? {:cube-1 {:entity/los 0}
                        :cube-2 {:entity/los 0}}
                       :cube-1 :cube-2)
 => true

 (provided
  (mc/cubes-between :cube-1 :cube-2) => [])


 (battlefield-visible? {:cube-1 {:entity/los 1}
                        :cube-2 {:entity/los 1}
                        :cube-3 {:entity/los 1}}
                       :cube-1 :cube-3)
 => false

 (provided
  (mc/cubes-between :cube-1 :cube-3) => [:cube-2])


 (battlefield-visible? {:cube-1 {:entity/los 1}
                        :cube-2 {:entity/los 0}
                        :cube-3 {:entity/los 1}}
                       :cube-1 :cube-3)
 => true

 (provided
  (mc/cubes-between :cube-1 :cube-3) => [:cube-2])


 (battlefield-visible? {:cube-1 {:entity/los 1}
                        :cube-2 {:entity/los 1}
                        :cube-3 {:entity/los 2}}
                       :cube-1 :cube-3)
 => true

 (provided
  (mc/cubes-between :cube-1 :cube-3) => [:cube-2]))


(facts
 "field of view"

 (field-of-view {:cube-1 {:unit/facing :n}} :cube-1)
 => []

 (provided
  (mc/forward-slice :cube-1 :n 1) => [])


 (field-of-view {:cube-1 {:unit/facing :n}} :cube-1)
 => []

 (provided
  (mc/forward-slice :cube-1 :n 1) => [:cube-2])


 (let [battlefield {:cube-1 {:unit/facing :n}
                    :cube-2 :entity-1}]

   (field-of-view battlefield :cube-1)
   => []

   (provided
    (mc/forward-slice :cube-1 :n 1) => [:cube-2]
    (battlefield-visible? battlefield :cube-1 :cube-2) => false))


 (let [battlefield {:cube-1 {:unit/facing :n}
                    :cube-2 :entity-1}]

   (field-of-view battlefield :cube-1)
   => [:cube-2]

   (provided
    (mc/forward-slice :cube-1 :n 1) => [:cube-2]
    (battlefield-visible? battlefield :cube-1 :cube-2) => true
    (mc/forward-slice :cube-1 :n 2) => [])))


(facts
 "valid pointer?"

 (valid-pointer? {} (mc/->Pointer :cube-1 :facing-1))
 => false

 (valid-pointer? {:cube-1 :unit-1} (mc/->Pointer :cube-1 :facing-1))
 => false

 (provided
  (lt/passable? :unit-1) => false)

 (valid-pointer? {:cube-1 :terrain-1} (mc/->Pointer :cube-1 :facing-1))
 => true

 (provided
  (lt/passable? :terrain-1) => true))
