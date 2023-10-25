(ns hexenhammer.transition.battlemap-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.battlemap :refer :all]))


(facts
 "set presentation"

 (set-presentation {:cube-1 {:entity/presentation :presentation-1}
                    :cube-2 {:entity/presentation :presentation-2}
                    :cube-3 {:entity/presentation :presentation-3}}
                   :presentation-4)
 => {:cube-1 {:entity/presentation :presentation-4}
     :cube-2 {:entity/presentation :presentation-4}
     :cube-3 {:entity/presentation :presentation-4}}


 (set-presentation {:cube-1 {:entity/presentation :presentation-1}
                    :cube-2 {:entity/presentation :presentation-2}
                    :cube-3 {:entity/presentation :presentation-3}}
                   [:cube-2 :cube-3]
                   :presentation-4)
 => {:cube-1 {:entity/presentation :presentation-1}
     :cube-2 {:entity/presentation :presentation-4}
     :cube-3 {:entity/presentation :presentation-4}})
