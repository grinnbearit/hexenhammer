(ns hexenhammer.render.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.render.svg :as rs]
            [hexenhammer.render.entity :as re]
            [hexenhammer.render.core :refer :all]))


(facts
 "render battlefield"

 (render-battlefield {:game/setup {:rows 3 :columns 4}
                      :game/battlefield {:cube-1 :entity-1
                                         :cube-2 :entity-2}})
 => [:svg :size-dim
     (list [:render :entity-1]
           [:render :entity-2])]

 (provided
  (rs/size->dim 3 4) => :size-dim
  (re/render :entity-1 :cube-1) => [:render :entity-1]
  (re/render :entity-2 :cube-2) => [:render :entity-2]))
