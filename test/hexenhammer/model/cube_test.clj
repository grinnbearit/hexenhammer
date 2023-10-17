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
 "neighbours within"

 (neighbours-within (->Cube 0 0 0) 1)
 => [(->Cube -1 0 1)
     (->Cube -1 1 0)
     (->Cube 0 -1 1)
     (->Cube 0 1 -1)
     (->Cube 1 -1 0)
     (->Cube 1 0 -1)])


(facts
 "rotate"

 (rotate (->Cube 0 -2 2))
 => (->Cube 2 -2 0)

 (rotate (->Cube 0 -2 2) 2)
 => (->Cube 2 0 -2)

 (rotate (->Cube 0 -2 2) 4)
 => (->Cube -2 2 0))


(facts
 "forward slice"

 (forward-slice (->Cube 0 0 0) :n 0)
 => [(->Cube 0 0 0)]


 (forward-slice (->Cube 0 0 0) :n 1)
 => [(->Cube -1 0 1)
     (->Cube 0 -1 1)
     (->Cube 1 -1 0)]


 (forward-slice (->Cube 0 0 0) :n 2)
 => [(->Cube -2 0 2)
     (->Cube -1 -1 2)
     (->Cube 0 -2 2)
     (->Cube 1 -2 1)
     (->Cube 2 -2 0)]


 (forward-slice (->Cube 0 0 0) :ne 1)
 => [(->Cube 0 -1 1)
     (->Cube 1 -1 0)
     (->Cube 1 0 -1)]


 (forward-slice (->Cube 1 -1 0) :n 1)
 => [(->Cube 0 -1 1)
     (->Cube 1 -2 1)
     (->Cube 2 -2 0)])


(facts
 "distance"

 (distance (->Cube 0 0 0) (->Cube 0 0 0)) => 0
 (distance (->Cube 0 0 0) (->Cube 1 -1 0)) => 1
 (distance (->Cube 0 0 0) (->Cube 0 2 -2)) => 2)


(facts
 "round"

 (round (->Cube 0 0 0)) => (->Cube 0 0 0)

 (round (->Cube 1/2 -1/4 -1/4)) => (->Cube 0 0 0)

 (round (->Cube -1/4 1/2 -1/4)) => (->Cube 0 0 0)

 (round (->Cube -1/4 -1/4 1/2)) => (->Cube 0 0 0))


(facts
 "cubes between"

 (cubes-between (->Cube 0 1 -1) (->Cube 0 -1 1)) => [(->Cube 0 0 0)]

 (cubes-between (->Cube 1 1 -2) (->Cube -1 -1 2))
 => [(->Cube 1 0 -1)
     (->Cube 0 0 0)
     (->Cube 0 -1 1)])


(facts
 "direction"

 (direction :cube-1 :cube-2)
 => :ne

 (provided
  (cubes-between :cube-1 :cube-2) => []
  (step :cube-1 :n) => :cube-3
  (step :cube-1 :ne) => :cube-2)


 (direction :cube-1 :cube-3)
 => :ne

 (provided
  (cubes-between :cube-1 :cube-3) => [:cube-2]
  (step :cube-1 :n) => :cube-4
  (step :cube-1 :ne) => :cube-2))


(facts
 "pointer step"

 (pointer-step (->Pointer :cube-1 :n))
 => (->Pointer :cube-2 :n)

 (provided
  (step :cube-1 :n) => :cube-2))
