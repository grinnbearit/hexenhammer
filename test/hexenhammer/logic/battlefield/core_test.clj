(ns hexenhammer.logic.battlefield.core_test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
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
 "visible?"

 (visible? {:cube-1 {:entity/los 0}
            :cube-2 {:entity/los 0}}
           :cube-1 :cube-2)
 => true

 (provided
  (lc/cubes-between :cube-1 :cube-2) => [])


 (visible? {:cube-1 {:entity/los 1}
            :cube-2 {:entity/los 1}
            :cube-3 {:entity/los 1}}
           :cube-1 :cube-3)
 => false

 (provided
  (lc/cubes-between :cube-1 :cube-3) => [:cube-2])


 (visible? {:cube-1 {:entity/los 1}
            :cube-2 {:entity/los 0}
            :cube-3 {:entity/los 1}}
           :cube-1 :cube-3)
 => true

 (provided
  (lc/cubes-between :cube-1 :cube-3) => [:cube-2])


 (visible? {:cube-1 {:entity/los 1}
            :cube-2 {:entity/los 1}
            :cube-3 {:entity/los 2}}
           :cube-1 :cube-3)
 => true

 (provided
  (lc/cubes-between :cube-1 :cube-3) => [:cube-2]))
