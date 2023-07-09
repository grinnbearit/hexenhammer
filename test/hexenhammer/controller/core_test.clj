(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.controller.core :refer :all]))


(facts
 "select setup"

 (select {:game/phase :setup
          :game/battlefield {:cube-1 {:entity/presentation :default
                                      :entity/interaction :interaction-1}}}
         :cube-1)
 => {:game/phase :setup
     :game/battlefield {:cube-1 {:entity/presentation :selected
                                 :entity/interaction :interaction-1}}}


 (select {:game/phase :setup
          :game/battlefield {:cube-2 {:entity/presentation :selected
                                      :entity/interaction :interaction-2}}}
         :cube-2)
 => {:game/phase :setup
     :game/battlefield {:cube-2 {:entity/presentation :default
                                 :entity/interaction :interaction-2}}})
