(ns hexenhammer.model.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.event :refer :all]))


(facts
 "dangerous"

 (dangerous :cube-1 1 "unit" 2)
 => {:event/class :dangerous
     :event/cube :cube-1
     :unit/player 1
     :entity/name "unit"
     :unit/id 2})


(facts
 "heavy casualties"

 (heavy-casualties :cube-1 1 "unit" 2)
 => {:event/class :heavy-casualties
     :event/cube :cube-1
     :unit/player 1
     :entity/name "unit"
     :unit/id 2})


(facts
 "panic"

 (panic :cube-1 1 "unit" 2)
 => {:event/class :panic
     :event/cube :cube-1
     :unit/player 1
     :entity/name "unit"
     :unit/id 2})


(facts
 "opportunity attack"

 (opportunity-attack :cube-1 1 "unit" 2 2 "unit" 2 3)
 => {:event/class :opportunity-attack
     :event/cube :cube-1
     :unit/player 1
     :entity/name "unit"
     :unit/id 2
     :event/source {:unit/player 2
                    :entity/name "unit"
                    :unit/id 2}
     :event/wounds 3})
