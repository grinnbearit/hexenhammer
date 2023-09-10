(ns hexenhammer.model.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.event :refer :all]))


(facts
 "dangerous"

 (dangerous 1 2)
 => {:event/class :dangerous
     :unit/player 1
     :unit/id 2})
