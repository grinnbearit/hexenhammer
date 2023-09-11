(ns hexenhammer.controller.dice-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.controller.dice :refer :all]))


(facts
 "roll!"

 (roll! 3) => [1 4 6]


 (provided
  (roll-die!) =streams=> [1 4 6]))


(facts
 "matches"

 (matches [1 2 3 4 5 6 1 2 3 4 5 6] 6) => 2)
