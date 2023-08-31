(ns hexenhammer.model.probability-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.probability :refer :all]))


(facts
 "march"

 (march 2) => 1/36
 (march 7) => 7/12
 (march 12) => 1)


(facts
 "charge"

 (charge 8)
 => {3 1
     4 8/9
     5 11/36}

 (charge 8 2) => 1
 (charge 8 4) => 8/9
 (charge 8 6) => 0)
