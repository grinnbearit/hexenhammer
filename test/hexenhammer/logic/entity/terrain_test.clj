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
 "passable?"

 (passable? :unit-1) => false

 (provided
  (terrain? :unit-1) => false)


 (let [entity {:terrain/type :impassable}]
   (passable? entity) => false

   (provided
    (terrain? entity) => true))


 (let [entity {:terrain/type :open}]
   (passable? entity) => true

   (provided
    (terrain? entity) => true)))


(facts
 "dangerous?"

 (dangerous? :terrain-1) => false
 (dangerous? {:terrain/type :dangerous}) => true)
