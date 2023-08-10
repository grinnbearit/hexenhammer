(ns hexenhammer.model.logic.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as cube]
            [hexenhammer.model.logic.core :refer :all]))


(facts
 "units engaged?"

 (engaged? {:unit/player 1} {:unit/player 2})
 => false


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
  (cube/neighbours :cube-1)
  => [:cube-2])


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 {:entity/class :terrain}}
                       :cube-1)
 => false

 (provided
  (cube/neighbours :cube-1)
  => [:cube-2])


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 {:entity/class :unit}}
                       :cube-1)
 => false

 (provided
  (cube/neighbours :cube-1)
  => [:cube-2]

  (engaged? :unit-1 {:entity/class :unit})
  => false)


 (battlefield-engaged? {:cube-1 :unit-1
                        :cube-2 {:entity/class :unit}}
                       :cube-1)
 => true

 (provided
  (cube/neighbours :cube-1)
  => [:cube-2]

  (engaged? :unit-1 {:entity/class :unit})
  => true))
