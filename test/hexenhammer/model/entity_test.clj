(ns hexenhammer.model.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.entity :refer :all]))


(facts
 "gen terrain"

 (gen-terrain :cube-1)
 => {:entity/class :terrain
     :entity/name "terrain"
     :entity/cube :cube-1
     :entity/presentation :default
     :entity/interaction :default}

 (gen-terrain :cube-1 :presentation :presentation-1 :interaction :interaction-1)
 => {:entity/class :terrain
     :entity/name "terrain"
     :entity/cube :cube-1
     :entity/presentation :presentation-1
     :entity/interaction :interaction-1})
