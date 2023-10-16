(ns hexenhammer.model.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.event :refer :all]))


(facts
 "dangerous"

 (dangerous :cube-1 :unit-key-1)
 => {:event/class :dangerous
     :event/cube :cube-1
     :event/unit-key :unit-key-1})


(facts
 "heavy casualties"

 (heavy-casualties :cube-1 :unit-key-1)
 => {:event/class :heavy-casualties
     :event/cube :cube-1
     :event/unit-key :unit-key-1})


(facts
 "panic"

 (panic :cube-1 :unit-key-1)
 => {:event/class :panic
     :event/cube :cube-1
     :event/unit-key :unit-key-1})


(facts
 "opportunity attack"

 (opportunity-attack :cube-1 :unit-key-1 3)
 => {:event/class :opportunity-attack
     :event/cube :cube-1
     :event/unit-key :unit-key-1
     :event/wounds 3})
