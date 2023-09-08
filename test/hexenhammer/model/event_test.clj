(ns hexenhammer.model.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.event :refer :all]))


(facts
 "dangerous"

 (dangerous)
 => {:event/class :dangerous})
