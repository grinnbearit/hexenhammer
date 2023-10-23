(ns hexenhammer.logic.entity.terrain-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.terrain :refer :all]))


(facts
 "gen open ground"

 (gen-open-ground)
 => {:entity/class :terrain
     :entity/presentation :default
     :entity/los 0
     :terrain/type :open})


(facts
 "terrain?"

 (terrain? {:entity/class :unit}) => false
 (terrain? {:entity/class :terrain}) => true)


(facts
 "place"

 (place :terrain-1 {})
 => {:unit/terrain :terrain-1})


(facts
 "clear"

 (clear {:unit/terrain :terrain-1})
 => :terrain-1)


(facts
 "pickup"

 (pickup {:unit/terrain :terrain-1})
 => {})
