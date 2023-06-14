(ns hexenhammer.cube-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.cube :refer :all]))


(facts
 "add"

 (add (->Cube 1 2 3)
      (->Cube 4 5 6))
 => (->Cube 5 7 9))


(facts
 "subtract"

 (subtract (->Cube 1 2 3)
           (->Cube 4 5 6))
 => (->Cube -3 -3 -3))


(facts
 "distance"

 (distance (->Cube 6 0 -6)
           (->Cube 7 1 -6))
 => 1

 (distance (->Cube 6 0 -6)
           (->Cube 9 -2 -7))
 => 3)


(facts
 "step"

 (step (->Cube 0 0 0) :n) => (->Cube 0 -1 1)
 (step (->Cube 0 0 0) :ne) => (->Cube 1 -1 0)
 (step (->Cube 0 0 0) :se) => (->Cube 1 0 -1)
 (step (->Cube 0 0 0) :s) => (->Cube 0 1 -1)
 (step (->Cube 0 0 0) :sw) => (->Cube -1 1 0)
 (step (->Cube 0 0 0) :nw) => (->Cube -1 0 1))
