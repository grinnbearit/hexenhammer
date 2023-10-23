(ns hexenhammer.logic.battlefield.unit
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.terrain :as let]
            [hexenhammer.logic.battlefield.unit :refer :all]))


(facts
 "remove unit"

 (let [battlefield {:cube-1 :unit-1
                    :cube-2 :terrain-2}]

   (remove-unit battlefield :cube-1)
   => {:cube-1 :terrain-1
       :cube-2 :terrain-2}

   (provided
    (let/clear :unit-1) => :terrain-1)))
