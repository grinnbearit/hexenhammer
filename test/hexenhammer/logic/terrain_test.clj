(ns hexenhammer.logic.terrain-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :refer :all]))


(facts
 "place"

 (place {} {:entity/class :terrain
            :entity/state :default})
 => {:object/terrain {:entity/class :terrain}})


(facts
 "pickup"

 (pickup :terrain-1) => :terrain-1

 (provided
  (le/terrain? :terrain-1) => true)


 (let [object {:object/terrain :terrain-1}]
   (pickup object) => :terrain-1

   (provided
    (le/terrain? object) => false)))


(facts
 "swap"

 (swap :object :entity) => :place

 (provided
  (place :object (pickup :entity)) => :place))


(facts
 "passable?"

 (passable? :unit-1) => false

 (provided
  (le/terrain? :unit-1) => false)


 (let [entity {:terrain/type :impassable}]
   (passable? entity) => false

   (provided
    (le/terrain? entity) => true))


 (let [entity {:terrain/type :open}]
   (passable? entity) => true

   (provided
    (le/terrain? entity) => true)))
