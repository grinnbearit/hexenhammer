(ns hexenhammer.controller.battlemap-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.controller.battlemap :refer :all]))


(facts
 "refresh battlemap"

 (refresh-battlemap {:game/battlefield {:cube-1 :terrain-1}
                     :game/battlemap {:cube-1 :unit-1}}
                    [:cube-1])
 => {:game/battlefield {:cube-1 :terrain-1}
     :game/battlemap {:cube-1 :terrain-1}})
