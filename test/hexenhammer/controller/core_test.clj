(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.entity :as entity]
            [hexenhammer.model.logic :as logic]
            [hexenhammer.controller.core :refer :all]))


(facts
 "select setup select-hex"

 (select {:game/phase :setup
          :game/subphase :select-hex
          :game/battlefield {:cube-1 {:entity/class :terrain
                                      :entity/presentation :default
                                      :entity/interaction :interaction-1}}}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :add-unit
     :game/selected :cube-1
     :game/battlefield {:cube-1 {:entity/class :terrain
                                 :entity/presentation :selected
                                 :entity/interaction :interaction-1}}}

 (select {:game/phase :setup
          :game/subphase :select-hex
          :game/battlefield {:cube-1 {:entity/class :unit
                                      :entity/presentation :default
                                      :entity/interaction :interaction-1}}}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :remove-unit
     :game/selected :cube-1
     :game/battlefield {:cube-1 {:entity/class :unit
                                 :entity/presentation :selected
                                 :entity/interaction :interaction-1}}})


(facts
 "unselect"

 (unselect {:game/subphase :add-unit
            :game/selected :cube-1
            :game/battlefield {:cube-1 {:entity/presentation :selected}}})
 => {:game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/presentation :default}}})


(facts
 "select setup add unit"

 (select {:game/phase :setup
          :game/subphase :add-unit
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:entity/presentation :selected}}}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/presentation :default}}}


 (select {:game/phase :setup
          :game/subphase :add-unit
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:entity/presentation :selected}
                             :cube-2 {:entity/class :terrain
                                      :entity/presentation :default}}}
         :cube-2)
 => {:game/phase :setup
     :game/subphase :add-unit
     :game/selected :cube-2
     :game/battlefield {:cube-1 {:entity/presentation :default}
                        :cube-2 {:entity/class :terrain
                                 :entity/presentation :selected}}})


(facts
 "select setup remove unit"

 (select {:game/phase :setup
          :game/subphase :remove-unit
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:entity/presentation :selected}}}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/presentation :default}}}


 (select {:game/phase :setup
          :game/subphase :remove-unit
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:entity/presentation :selected}
                             :cube-2 {:entity/class :terrain
                                      :entity/presentation :default}}}
         :cube-2)
 => {:game/phase :setup
     :game/subphase :add-unit
     :game/selected :cube-2
     :game/battlefield {:cube-1 {:entity/presentation :default}
                        :cube-2 {:entity/class :terrain
                                 :entity/presentation :selected}}})


(facts
 "add unit"

 (add-unit {:game/selected :cube-1
            :game/battlefield {:cube-1 :terrain-1}
            :game/units {1 {:counter 0 :cubes {}}}}
           1
           :facing-1)
 => {:game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/class :unit
                                 :entity/presentation :default}}
     :game/units {1 {:counter 1 :cubes {1 :cube-1}}}}

 (provided
  (entity/gen-unit :cube-1 1 1 :facing-1 :interaction :selectable)
  => {:entity/class :unit}))


(facts
 "remove unit"

 (remove-unit {:game/selected :cube-1
               :game/battlefield {:cube-1 {:unit/player 1
                                           :unit/id 1}}
               :game/units {1 {:counter 1 :cubes {1 :cube-1}}}})
 => {:game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/class :terrain
                                 :entity/presentation :default}}
     :game/units {1 {:counter 1 :cubes {}}}}

 (provided
  (entity/gen-terrain :cube-1 :interaction :selectable)
  => {:entity/class :terrain}))


(facts
 "to movement"

 (let [battlefield {:cube-1 {}
                    :cube-2 {:unit/player 2}
                    :cube-3 {:unit/player 1}}]

   (to-movement {:game/player 1
                 :game/battlefield battlefield})
   => {:game/player 1
       :game/phase :movement
       :game/subphase :select-hex
       :game/battlefield {:cube-1 {:entity/interaction :default}
                          :cube-2 {:unit/player 2
                                   :entity/interaction :default}
                          :cube-3 {:unit/player 1
                                   :entity/interaction :default}}}

   (provided
    (logic/battlefield-engaged? battlefield {:unit/player 1})
    => true)


   (to-movement {:game/player 1
                 :game/battlefield battlefield})
   => {:game/player 1
       :game/phase :movement
       :game/subphase :select-hex
       :game/battlefield {:cube-1 {:entity/interaction :default}
                          :cube-2 {:unit/player 2
                                   :entity/interaction :default}
                          :cube-3 {:unit/player 1
                                   :entity/presentation :highlighted
                                   :entity/interaction :selectable}}}

   (provided
    (logic/battlefield-engaged? battlefield {:unit/player 1})
    => false)))


(facts
 "select movement select-hex"

 (select {:game/phase :movement
          :game/subphase :select-hex
          :game/player :player-1
          :game/battlefield {:cube-1 {:unit/facing :facing-1}}}
         :cube-1)

 => {:game/phase :movement
     :game/subphase :reform
     :game/selected :cube-1
     :game/player :player-1
     :game/battlefield {:cube-1 {:unit/facing :facing-1}}
     :game/battlemap {:cube-1 :mover-1}}

 (provided
  (entity/gen-mover :cube-1 :player-1 :facing-1
                    :presentation :selected)
  => :mover-1))


(facts
 "select movement reform"

 (select {:game/phase :movement
          :game/subphase :reform
          :game/player :player-1
          :game/battlefield {:cube-1 {:unit/facing :facing-1}}}
         :cube-1)
 => {:game/phase :movement
     :game/subphase :reform
     :game/player :player-1
     :game/battlefield {:cube-1 {:unit/facing :facing-1}}
     :game/selected :cube-1
     :game/battlemap {:cube-1 :mover-1}}

 (provided
  (entity/gen-mover :cube-1 :player-1 :facing-1 :presentation :selected)
  => :mover-1))
