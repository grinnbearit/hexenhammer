(ns hexenhammer.render.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.render.svg :as rs]
            [hexenhammer.render.entity :refer :all]))


(facts
 "render base"

 (render-base {:terrain/type :open
               :entity/presentation :default})
 => [:hexagon {:class "terrain open default"}]

 (provided
  (rs/hexagon) => [:hexagon {}]))


(facts
 "render terrain"

 (let [entity {:entity/class :terrain
               :entity/presentation :default}]

   (render entity :phase-1 :cube-1)
   => :if-selectable

   (provided
    (render-base entity) => :render-base

    (rs/translate :render-base :cube-1) => :translate

    (rs/if-selectable :translate :default :phase-1 :cube-1) => :if-selectable)))
