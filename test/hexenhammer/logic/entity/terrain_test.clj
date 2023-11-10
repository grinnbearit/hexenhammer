(ns hexenhammer.logic.entity.terrain-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.terrain :refer :all]))


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


(facts
 "dangerous?"

 (dangerous? :terrain-1) => false
 (dangerous? {:terrain/type :dangerous}) => true)


(facts
 "impassable?"

 (impassable? :terrain-1) => false
 (impassable? {:terrain/type :impassable}) => true)


(facts
 "passable?"

 (passable? :entity-1) => false

 (provided
  (terrain? :entity-1) => false)


 (passable? :terrain-1) => false

 (provided
  (terrain? :terrain-1) => true
  (impassable? :terrain-1) => true)


 (passable? :terrain-1) => true

 (provided
  (terrain? :terrain-1) => true
  (impassable? :terrain-1) => false))
