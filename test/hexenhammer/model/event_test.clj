(ns hexenhammer.model.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.event :refer :all]))


(facts
 "dangerous"

 (dangerous 1 "unit" 2)
 => {:event/class :dangerous
     :unit/player 1
     :entity/name "unit"
     :unit/id 2})


(facts
 "panic"

 (panic 1 "unit" 2)
 => {:event/class :panic
     :unit/player 1
     :entity/name "unit"
     :unit/id 2})
