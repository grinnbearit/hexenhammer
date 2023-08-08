(ns hexenhammer.controller.battlefield-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.controller.entity :as ce]
            [hexenhammer.controller.battlefield :refer :all]))


(facts
 "reset default"

 (reset-default {:cube-1 :entity-1
                 :cube-2 :entity-2})
 => {:cube-1 :reset-entity-1
     :cube-2 :reset-entity-2}

 (provided
  (ce/reset-default :entity-1) => :reset-entity-1
  (ce/reset-default :entity-2) => :reset-entity-2))


(facts
 "set interactable"

 (set-interactable {:cube-1 :entity-1
                    :cube-2 :entity-2
                    :cube-3 :entity-3}
                   [:cube-2 :cube-3])
 => {:cube-1 :entity-1
     :cube-2 :interactable-entity-2
     :cube-3 :interactable-entity-3}

 (provided
  (ce/set-interactable :entity-2) => :interactable-entity-2
  (ce/set-interactable :entity-3) => :interactable-entity-3))
