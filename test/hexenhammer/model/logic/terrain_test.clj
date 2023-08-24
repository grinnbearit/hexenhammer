(ns hexenhammer.model.logic.terrain-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.logic.entity :as mle]
            [hexenhammer.model.logic.terrain :refer :all]))


(facts
 "place"

 (place {} {:entity/class :terrain
            :entity/state :default})
 => {:object/terrain {:entity/class :terrain}})


(facts
 "pickup"

 (pickup :terrain-1) => :terrain-1

 (provided
  (mle/terrain? :terrain-1) => true)


 (let [object {:object/terrain :terrain-1}]
   (pickup object) => :terrain-1

   (provided
    (mle/terrain? object) => false)))


(facts
 "swap"

 (swap :object :entity) => :place

 (provided
  (place :object (pickup :entity)) => :place))
