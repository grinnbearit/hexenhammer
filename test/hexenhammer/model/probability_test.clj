(ns hexenhammer.model.probability-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.probability :refer :all]))


(facts
 "march"

 (march 2) => 1/36
 (march 7) => 7/12
 (march 12) => 1)
