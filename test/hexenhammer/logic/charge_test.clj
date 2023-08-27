(ns hexenhammer.logic.charge-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.core :as l]
            [hexenhammer.logic.charge :refer :all]))


(facts
 "show charge"

 (let [battlefield {:cube-1 {} :cube-2 {} :cube-3 {}}]

   (show-charge battlefield :cube-4)
   =>    {:cube-1 {:entity/state :marked}
          :cube-2 {:entity/state :marked}}

   (provided
    (l/field-of-view battlefield :cube-4)
    => [:cube-1 :cube-2])))
