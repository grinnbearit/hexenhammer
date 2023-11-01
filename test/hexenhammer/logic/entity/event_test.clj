(ns hexenhammer.logic.entity.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.event :refer :all]))


(facts
 "dangerous"

 (dangerous :cube-1 :unit-key-1)
 => {:entity/class :event
     :event/type :dangerous
     :event/cube :cube-1
     :event/unit-key :unit-key-1})
