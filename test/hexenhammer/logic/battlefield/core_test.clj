(ns hexenhammer.logic.battlefield.core_test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.core :refer :all]))


(facts
 "gen battlefield cubes"

 (gen-battlefield-cubes 0 0)
 => []

 (gen-battlefield-cubes 1 2)
 => [(lc/->Cube 0 0 0)
     (lc/->Cube 1 0 -1)]

 (gen-battlefield-cubes 2 4)
 => [(lc/->Cube 0 0 0)
     (lc/->Cube 1 0 -1)
     (lc/->Cube 2 -1 -1)
     (lc/->Cube 3 -1 -2)

     (lc/->Cube 0 1 -1)
     (lc/->Cube 1 1 -2)
     (lc/->Cube 2 0 -2)
     (lc/->Cube 3 0 -3)])


(facts
 "gen initial state"

 (gen-initial-state 3 4)
 => {:game/setup {:rows 3
                  :columns 4}
     :game/battlefield {:cube-1 :terrain-1
                        :cube-2 :terrain-1}
     :game/events []}

 (provided
  (gen-battlefield-cubes 3 4) => [:cube-1 :cube-2]
  (let/gen-open-ground) => :terrain-1))
