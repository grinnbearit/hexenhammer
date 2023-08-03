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
 "mark movable"

 (let [unit-1 {:unit/id 1
               :entity/presentation :default
               :entity/interaction :default}
       unit-2 {:unit/id 2
               :entity/presentation :default
               :entity/interaction :default}
       unit-3 {:unit/id 3
               :entity/presentation :default
               :entity/interaction :default}

       battlefield {:cube-1 unit-1
                    :cube-2 unit-2
                    :cube-3 unit-3}]

   (mark-movable battlefield [:cube-2 :cube-3])
   => {:cube-1 unit-1
       :cube-2 unit-2
       :cube-3 {:unit/id 3
                :entity/presentation :highlighted
                :entity/interaction :selectable}}

   (provided
    (logic/battlefield-engaged? battlefield :cube-2)
    => true

    (logic/battlefield-engaged? battlefield :cube-3)
    => false)))
