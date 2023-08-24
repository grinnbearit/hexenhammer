(ns hexenhammer.controller.battlefield-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.controller.battlefield :refer :all]))


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
