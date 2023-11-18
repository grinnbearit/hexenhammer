(ns hexenhammer.transition.state.battlemap-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.state.battlemap :refer :all]))


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


(facts
 "refill battlemap"

 (let [battlefield {:cube-1 :entity-1
                    :cube-2 :entity-2
                    :cube-3 :entity-3}
       battlemap {:cube-2 :entity-3}
       state {:game/battlefield battlefield
              :game/battlemap battlemap}]

   (refill-battlemap state [:cube-1 :cube-2])
   => {:game/battlefield battlefield
       :game/battlemap {:cube-1 :entity-1
                        :cube-2 :entity-3}}))
