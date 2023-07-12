(ns hexenhammer.model.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as cube]
            [hexenhammer.model.entity :as entity]
            [hexenhammer.model.core :refer :all]))


(facts
 "gen battlefield cubes"

 (gen-battlefield-cubes 0 0)
 => []

 (gen-battlefield-cubes 1 2)
 => [(cube/->Cube 0 0 0)
     (cube/->Cube 1 0 -1)]

 (gen-battlefield-cubes 2 4)
 => [(cube/->Cube 0 0 0)
     (cube/->Cube 1 0 -1)
     (cube/->Cube 2 -1 -1)
     (cube/->Cube 3 -1 -2)

     (cube/->Cube 0 1 -1)
     (cube/->Cube 1 1 -2)
     (cube/->Cube 2 0 -2)
     (cube/->Cube 3 0 -3)])


(facts
 "gen initial state"

 (gen-initial-state 3 4)
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/player 1
     :game/rows 3
     :game/columns 4
     :game/battlefield {:cube-1 :entity-1
                        :cube-2 :entity-2}}

 (provided
  (gen-battlefield-cubes 3 4) => [:cube-1 :cube-2]
  (entity/gen-terrain :cube-1 :interaction :selectable) => :entity-1
  (entity/gen-terrain :cube-2 :interaction :selectable) => :entity-2))