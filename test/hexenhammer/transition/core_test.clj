(ns hexenhammer.transition.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.core :as lb]
            [hexenhammer.transition.core :refer :all]))


(facts
 "gen initial state"

 (gen-initial-state 3 4)
 => {:game/setup {:rows 3
                  :columns 4}
     :game/units {1 {} 2 {}}
     :game/battlefield {:cube-1 let/OPEN-GROUND
                        :cube-2 let/OPEN-GROUND}
     :game/events []}

 (provided
  (lb/gen-battlefield-cubes 3 4) => [:cube-1 :cube-2]))
