(ns hexenhammer.controller.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.controller.entity :refer :all]))


(facts
 "reset default"

 (reset-default {})
 => {:entity/presentation :default
     :entity/interaction :default})


(facts
 "set interactable"

 (set-interactable {})
 => {:entity/presentation :highlighted
     :entity/interaction :selectable})
