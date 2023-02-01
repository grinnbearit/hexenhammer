(ns hexenhammer.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.cube :as cube]
            [hexenhammer.transition :refer :all]))


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
 => {:map/rows 3
     :map/columns 4
     :map/battlefield {(cube/->Cube 0 0 0) {:hexenhammer/class :terrain
                                            :terrain/name :grass}
                       (cube/->Cube 1 0 -1) {:hexenhammer/class :terrain
                                             :terrain/name :grass}}}

 (provided
  (gen-battlefield-cubes 3 4)
  => [(cube/->Cube 0 0 0)
      (cube/->Cube 1 0 -1)]))


(facts
 "place unit"

 (place-unit {:map/battlefield {(cube/->Cube 0 0 0) :grass}}
             (cube/->Cube 0 0 0)
             :unit)
 => {:map/battlefield {(cube/->Cube 0 0 0) :unit}})
