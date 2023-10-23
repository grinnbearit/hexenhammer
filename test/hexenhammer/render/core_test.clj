(ns hexenhammer.render.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.render.svg :as rs]
            [hexenhammer.render.entity :as re]
            [hexenhammer.render.core :refer :all]))


(facts
 "entity -> z"

 (entity->z {:entity/presentation :default}) => 0
 (entity->z {:entity/presentation :selectable}) => 1
 (entity->z {:entity/presentation :marked}) => 2
 (entity->z {:entity/presentation :selected}) => 3)


(facts
 "render battlefield"

 (render-battlefield {:game/setup {:rows 3 :columns 4}
                      :game/phase [:phase :subphase]
                      :game/battlefield {:cube-1 :entity-1
                                         :cube-2 :entity-2}
                      :game/battlemap {:cube-2 :entity-3}})
 => [:svg :size-dim
     (list [:render :entity-3]
           [:render :entity-1])]

 (provided
  (rs/size->dim 3 4) => :size-dim
  (entity->z :entity-1) => 2
  (entity->z :entity-3) => 1
  (re/render :entity-1 [:phase :subphase] :cube-1) => [:render :entity-1]
  (re/render :entity-3 [:phase :subphase] :cube-2) => [:render :entity-3]))
