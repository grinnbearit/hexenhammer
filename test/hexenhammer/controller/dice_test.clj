(ns hexenhammer.controller.dice-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.controller.dice :refer :all]))


(facts
 "roll!"

 (roll! 3) => [1 4 6]


 (provided
  (roll-die!) =streams=> [1 4 6]))
