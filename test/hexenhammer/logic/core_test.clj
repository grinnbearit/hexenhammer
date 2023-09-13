(ns hexenhammer.logic.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.core :refer :all]))


(facts
 "set state"

 (set-state {:cube-1 {:entity/state :state-1}
             :cube-2 {:entity/state :state-2}
             :cube-3 {:entity/state :state-3}}
            :state-4)
 => {:cube-1 {:entity/state :state-4}
     :cube-2 {:entity/state :state-4}
     :cube-3 {:entity/state :state-4}}


 (set-state {:cube-1 {:entity/state :state-1}
             :cube-2 {:entity/state :state-2}
             :cube-3 {:entity/state :state-3}}
            [:cube-2 :cube-3]
            :state-4)
 => {:cube-1 {:entity/state :state-1}
     :cube-2 {:entity/state :state-4}
     :cube-3 {:entity/state :state-4}})


(facts
 "show cubes"

 (show-cubes {:cube-1 :entity-1
              :cube-2 :entity-2
              :cube-3 :entity-3}
             [:cube-1 :cube-2]
             :selected)
 => :set-state

 (provided
  (set-state {:cube-1 :entity-1
              :cube-2 :entity-2}
             :selected)
  => :set-state))
