(ns hexenhammer.transition.dice-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.dice :refer :all]))


(facts
 "roll!"

 (roll! 3) => [1 4 6]


 (provided
  (roll-die!) =streams=> [1 4 6]))


(facts
 "matches"

 (matches [1 2 3 4 5 6 1 2 3 4 5 6] 6) => 2)
