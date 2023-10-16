(ns hexenhammer.view.widget-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :as entity]
            [hexenhammer.view.widget :refer :all]))


(facts
 "entity -> z"

 (entity->z {:entity/state :default}) => 0
 (entity->z {:entity/state :selectable}) => 1
 (entity->z {:entity/state :marked}) => 2
 (entity->z {:entity/state :selected}) => 3)


(facts
 "render battlefield"

 (render-battlefield {:game/rows 3
                      :game/columns 4
                      :game/battlefield {:cube-1 :entity-1
                                         :cube-2 :entity-2}
                      :game/battlemap {:cube-2 :entity-3}})
 => [:svg [:svg-size->dim 3 4]
     (list [:render :entity-3]
           [:render :entity-1])]

 (provided
  (svg/size->dim 3 4) => [:svg-size->dim 3 4]
  (entity->z :entity-1) => 2
  (entity->z :entity-3) => 1
  (entity/render :entity-1) => [:render :entity-1]
  (entity/render :entity-3) => [:render :entity-3]))


(facts
 "unit-key -> str"

 (unit-key->str {:unit/player 1
                 :entity/name "unit"
                 :unit/id 2})
 => "P1 - unit (ii)")
