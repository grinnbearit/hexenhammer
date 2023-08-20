(ns hexenhammer.model.cube-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :refer :all]))


(facts
 "add"

 (add (->Cube 1 2 3)
      (->Cube 4 5 6))
 => (->Cube 5 7 9))


(facts
 "step"

 (step (->Cube 0 0 0) :n) => (->Cube 0 -1 1)
 (step (->Cube 0 0 0) :ne) => (->Cube 1 -1 0)
 (step (->Cube 0 0 0) :se) => (->Cube 1 0 -1)
 (step (->Cube 0 0 0) :s) => (->Cube 0 1 -1)
 (step (->Cube 0 0 0) :sw) => (->Cube -1 1 0)
 (step (->Cube 0 0 0) :nw) => (->Cube -1 0 1))


(facts
 "forward arc"

 (forward-arc (->Cube 0 0 0) :n) => [(->Cube -1 0 1)
                                     (->Cube 0 -1 1)
                                     (->Cube 1 -1 0)]

 (forward-arc (->Cube 0 0 0) :s) => [(->Cube 1 0 -1)
                                     (->Cube 0 1 -1)
                                     (->Cube -1 1 0)])


(facts
 "neighbours"

 (neighbours (->Cube 0 0 0))
 => [(->Cube 0 -1 1)
     (->Cube 1 -1 0)
     (->Cube 1 0 -1)
     (->Cube 0 1 -1)
     (->Cube -1 1 0)
     (->Cube -1 0 1)])


(facts
 "neighbours at"

 (neighbours-at (->Cube 0 0 0) 2)
 => [(->Cube 2 0 -2)
     (->Cube 2 -2 0)
     (->Cube 0 2 -2)
     (->Cube 0 -2 2)
     (->Cube -2 2 0)
     (->Cube -2 0 2)
     (->Cube 2 -1 -1)
     (->Cube -1 2 -1)
     (->Cube -1 -1 2)
     (->Cube -2 1 1)
     (->Cube 1 -2 1)
     (->Cube 1 1 -2)])


(facts
 "neighbours within"

 (neighbours-within :cube-1 2)
 => [:neighbours-1 :neighbours-2]

 (provided
  (neighbours-at :cube-1 1) => [:neighbours-1]
  (neighbours-at :cube-1 2) => [:neighbours-2]))
