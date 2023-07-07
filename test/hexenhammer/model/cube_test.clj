(ns hexenhammer.model.cube-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :refer :all]))


(facts
 "add"

 (add (->Cube 1 2 3)
      (->Cube 4 5 6))
 => (->Cube 5 7 9))
