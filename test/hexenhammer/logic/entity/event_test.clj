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
