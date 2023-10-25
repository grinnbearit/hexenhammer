(ns hexenhammer.transition.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.core :refer :all]))


(facts
 "reset battlemap"

 (reset-battlemap {:game/battlefield :battlefield-1})
 => {:game/battlefield :battlefield-1
     :game/battlemap :battlefield-1}


 (reset-battlemap {:game/battlefield {:cube-1 :entity-1
                                      :cube-2 :entity-2}
                   :game/battlemap :battlemap-1}
                  [:cube-1])

 => {:game/battlefield {:cube-1 :entity-1
                        :cube-2 :entity-2}
     :game/battlemap {:cube-1 :entity-1}})


(facts
 "refresh battlemap"

 (refresh-battlemap {:game/battlefield {:cube-1 :terrain-1
                                        :cube-2 :terrain-2}
                     :game/battlemap {:cube-1 :unit-1
                                      :cube-2 :unit-2}}
                    [:cube-1])
 => {:game/battlefield {:cube-1 :terrain-1
                        :cube-2 :terrain-2}
     :game/battlemap {:cube-1 :terrain-1
                      :cube-2 :unit-2}})
