(ns hexenhammer.render.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.render.svg :as rs]
            [hexenhammer.render.entity :refer :all]))


(facts
 "render terrain"

 (let [entity {:entity/class :terrain
               :terrain/type :open}]

   (render entity :cube-1)
   => :translate

   (provided
    (rs/hexagon) => [:terrain {}]

    (rs/translate [:terrain {:class "terrain open"}] :cube-1) => :translate)))
