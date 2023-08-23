(ns hexenhammer.model.logic.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as cube]
            [hexenhammer.model.logic.entity :as mle]
            [hexenhammer.model.logic.core :refer :all]))


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
  (cube/forward-arc :cube-1 :n)
  => [:cube-3 :cube-4]

  (cube/forward-arc :cube-2 :n)
  => [:cube-5 :cube-6])


 (engaged? {:unit/player 1
            :entity/cube :cube-1
            :unit/facing :n}
           {:unit/player 2
            :entity/cube :cube-2
            :unit/facing :n})
 => true

 (provided
  (cube/forward-arc :cube-1 :n)
  => [:cube-2])


 (engaged? {:unit/player 1
            :entity/cube :cube-1
            :unit/facing :n}
           {:unit/player 2
            :entity/cube :cube-2
            :unit/facing :n})
 => true

 (provided
  (cube/forward-arc :cube-1 :n)
  => [:cube-3 :cube-4]

  (cube/forward-arc :cube-2 :n)
  => [:cube-1]))


(facts
 "battlefield engaged?"

 (battlefield-engaged? {:cube-1 :unit-1} :cube-1)
 => false

 (provided
  (cube/neighbours :cube-1) => [])


 (battlefield-engaged? {:cube-1 :unit-1} :cube-1)
 => false

 (provided
  (cube/neighbours :cube-1) => [:cube-2])


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 :terrain-1}
                       :cube-1)
 => false

 (provided
  (cube/neighbours :cube-1) => [:cube-2]

  (mle/unit? :terrain-1) => false)


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 :unit-2}
                       :cube-1)
 => false

 (provided
  (cube/neighbours :cube-1) => [:cube-2]

  (mle/unit? :unit-2) => true

  (engaged? :unit-1 :unit-2) => false)


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 :unit-2}
                       :cube-1)
 => true

 (provided
  (cube/neighbours :cube-1) => [:cube-2]

  (mle/unit? :unit-2) => true

  (engaged? :unit-1 :unit-2) => true))


(facts
 "battlefield visible?"

 (battlefield-visible? :battlefield :cube-1 :cube-2)
 => true

 (provided
  (cube/cubes-between :cube-1 :cube-2) => [])


 (battlefield-visible? :battlefield :cube-1 :cube-2)
 => false

 (provided
  (cube/cubes-between :cube-1 :cube-2) => [:cube-1])


 (battlefield-visible? {:cube-3 :unit-1} :cube-1 :cube-2)
 => false

 (provided
  (cube/cubes-between :cube-1 :cube-2) => [:cube-3]
  (mle/terrain? :unit-1) => false)


 (battlefield-visible? {:cube-3 :terrain-1} :cube-1 :cube-2)
 => true

 (provided
  (cube/cubes-between :cube-1 :cube-2) => [:cube-3]
  (mle/terrain? :terrain-1) => true))


(facts
 "field of view"

 (field-of-view {:cube-1 {:unit/facing :n}} :cube-1)
 => []

 (provided
  (cube/forward-slice :cube-1 :n 1) => [])


 (field-of-view {:cube-1 {:unit/facing :n}} :cube-1)
 => []

 (provided
  (cube/forward-slice :cube-1 :n 1) => [:cube-2])


 (let [battlefield {:cube-1 {:unit/facing :n}
                    :cube-2 :entity-1}]

   (field-of-view battlefield :cube-1)
   => []

   (provided
    (cube/forward-slice :cube-1 :n 1) => [:cube-2]
    (battlefield-visible? battlefield :cube-1 :cube-2) => false))


 (let [battlefield {:cube-1 {:unit/facing :n}
                    :cube-2 :entity-1}]

   (field-of-view battlefield :cube-1)
   => [:cube-2]

   (provided
    (cube/forward-slice :cube-1 :n 1) => [:cube-2]
    (battlefield-visible? battlefield :cube-1 :cube-2) => true
    (cube/forward-slice :cube-1 :n 2) => [])))
