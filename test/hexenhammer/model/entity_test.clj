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


(facts
 "gen unit"

 (gen-unit :cube-1 :player-1 :facing-1)
 => {:entity/class :unit
     :entity/name "unit"
     :entity/cube :cube-1
     :unit/player :player-1
     :unit/facing :facing-1
     :entity/presentation :default
     :entity/interaction :default}

 (gen-unit :cube-1 :player-1 :facing-1 :presentation :presentation-1 :interaction :interaction-1)
 => {:entity/class :unit
     :entity/name "unit"
     :entity/cube :cube-1
     :unit/player :player-1
     :unit/facing :facing-1
     :entity/presentation :presentation-1
     :entity/interaction :interaction-1})
