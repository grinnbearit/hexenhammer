(ns hexenhammer.logic.entity.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.event :refer :all]))


(facts
 "dangerous terrain"

 (dangerous-terrain :cube-1 :unit-key-1)
 => {:entity/class :event
     :event/type :dangerous-terrain
     :event/cube :cube-1
     :event/unit-key :unit-key-1})


(facts
 "heavy casualties"

 (heavy-casualties :cube-1 :unit-key-1)
 => {:entity/class :event
     :event/type :heavy-casualties
     :event/cube :cube-1
     :event/unit-key :unit-key-1})


(facts
 "opportunity attack"

 (opportunity-attack :cube-1 :unit-key-1 10)
 => {:entity/class :event
     :event/type :opportunity-attack
     :event/cube :cube-1
     :event/unit-key :unit-key-1
     :event/wounds 10})


(facts
 "panic"

 (panic :unit-key-1)
 => {:entity/class :event
     :event/type :panic
     :event/unit-key :unit-key-1})
