(ns hexenhammer.controller.battlefield-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.logic :as logic]
            [hexenhammer.controller.battlefield :refer :all]))


(facts
 "reset default"

 (reset-default {:cube-1 {:entity/presentation :selected
                          :entity/interaction :selectable}})
 => {:cube-1 {:entity/presentation :default
              :entity/interaction :default}})


(facts
 "set interactable"

 (let [unit-1 {:unit/id 1
               :entity/presentation :default
               :entity/interaction :default}
       unit-2 {:unit/id 2
               :entity/presentation :default
               :entity/interaction :default}

       battlefield {:cube-1 unit-1
                    :cube-2 unit-2}]

   (set-interactable battlefield [:cube-2])
   => {:cube-1 unit-1
       :cube-2 {:unit/id 2
                :entity/presentation :highlighted
                :entity/interaction :selectable}}))
