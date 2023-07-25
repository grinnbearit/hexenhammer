(ns hexenhammer.view.html-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :as entity]
            [hexenhammer.view.html :refer :all]))


(facts
 "entity -> z"

 (entity->z {:entity/presentation :default}) => 0
 (entity->z {:entity/presentation :highlighted}) => 1
 (entity->z {:entity/presentation :selected}) => 2)


(facts
 "render battlefield"

 (render-battlefield {:game/rows 3
                      :game/columns 4
                      :game/battlefield {:cube-1 :entity-1
                                         :cube-2 :entity-2}})
 => [:svg [:svg-size->dim 3 4]
     (list [:render :entity-2]
           [:render :entity-1])]

 (provided
  (svg/size->dim 3 4) => [:svg-size->dim 3 4]
  (entity->z :entity-1) => 2
  (entity->z :entity-2) => 1
  (entity/render :entity-1) => [:render :entity-1]
  (entity/render :entity-2) => [:render :entity-2]))
