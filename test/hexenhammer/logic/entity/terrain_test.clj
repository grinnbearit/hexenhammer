(ns hexenhammer.logic.entity.terrain-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.terrain :refer :all]))


(facts
 "gen open ground"

 (gen-open-ground :cube-1)
 => {:entity/class :terrain
     :entity/los 0
     :terrain/type :open})
