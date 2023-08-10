(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.controller.entity :as ce]
            [hexenhammer.controller.battlefield :as cb]
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
  (me/gen-unit :cube-1 1 1 :facing-1 :interaction :selectable)
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
  (me/gen-terrain :cube-1 :interaction :selectable)
  => {:entity/class :terrain}))


(facts
 "to movement"

 (to-movement {:game/player 1
               :game/battlefield :battlefield-1
               :game/units {1 {:cubes {1 :unit-cube-1
                                       2 :unit-cube-2}}}})
 => {:game/player 1
     :game/units {1 {:cubes {1 :unit-cube-1
                             2 :unit-cube-2}}}
     :game/phase :movement
     :game/subphase :select-hex
     :game/battlefield :battlefield-3}

 (provided
  (mlc/battlefield-engaged? :battlefield-1 :unit-cube-1)
  => false

  (mlc/battlefield-engaged? :battlefield-1 :unit-cube-2)
  => true

  (cb/reset-default :battlefield-1)
  => :battlefield-2

  (cb/set-interactable :battlefield-2 [:unit-cube-1])
  => :battlefield-3))


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
  (me/gen-mover :cube-1 :player-1 :facing-1
                :presentation :selected)
  => :mover-1))


(facts
 "select movement reform"

 (select {:game/phase :movement
          :game/subphase :reform
          :game/player :player-1
          :game/battlefield {:cube-1 {:unit/facing :facing-1}}
          :game/movement? true}
         :cube-1)
 => {:game/phase :movement
     :game/subphase :reform
     :game/player :player-1
     :game/battlefield {:cube-1 {:unit/facing :facing-1}}
     :game/selected :cube-1
     :game/battlemap {:cube-1 :mover-1}}

 (provided
  (me/gen-mover :cube-1 :player-1 :facing-1 :presentation :selected)
  => :mover-1))


(facts
 "skip movement"

 (skip-movement {:game/selected :cube-1
                 :game/battlefield {:cube-1 :entity-1}
                 :game/battlemap :battlemap})
 => {:game/battlefield {:cube-1 :reset-entity-1}
     :game/subphase :select-hex}

 (provided
  (ce/reset-default :entity-1) => :reset-entity-1))


(facts
 "move movement reform"

 (move {:game/phase :movement
        :game/subphase :reform
        :game/battlefield {:cube-1 {:unit/facing :n}}}
       :cube-1
       :s)
 => {:game/phase :movement
     :game/subphase :reform
     :game/battlefield {:cube-1 {:unit/facing :n}}
     :game/battlemap {:cube-1 {:unit/facing :s}}
     :game/movement? true}

 (move {:game/phase :movement
        :game/subphase :reform
        :game/battlefield {:cube-1 {:unit/facing :n}}
        :game/battlemap {:cube-1 {:unit/facing :s}}
        :game/movement? true}
       :cube-1
       :n)
 => {:game/phase :movement
     :game/subphase :reform
     :game/battlefield {:cube-1 {:unit/facing :n}}
     :game/battlemap {:cube-1 {:unit/facing :n}}})


(facts
 "finish movement"

 (finish-movement {:game/selected :cube-1
                   :game/battlefield {:cube-1 :unit-1}
                   :game/battlemap {:cube-1 {:unit/facing :n}}
                   :game/movement? true})
 => {:game/battlefield {:cube-1 {:unit/facing :n}}
     :game/subphase :select-hex}

 (provided
  (ce/reset-default :unit-1)
  => {}))
