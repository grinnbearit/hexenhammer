(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.logic.movement :as mlm]
            [hexenhammer.controller.entity :as ce]
            [hexenhammer.controller.battlefield :as cb]
            [hexenhammer.controller.movement :as cm]
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
 "unselect setup"

 (unselect {:game/phase :setup
            :game/selected :cube-1
            :game/battlefield {:cube-1 {:entity/presentation :selected}}})
 => {:game/phase :setup
     :game/subphase :select-hex
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

 (add-unit {:game/phase :setup
            :game/selected :cube-1
            :game/battlefield {:cube-1 :terrain-1}
            :game/units {1 {:counter 0 :cubes {}}}}
           1
           :facing-1)
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/class :unit
                                 :entity/presentation :default}}
     :game/units {1 {:counter 1 :cubes {1 :cube-1}}}}

 (provided
  (me/gen-unit :cube-1 1 1 :facing-1 :interaction :selectable)
  => {:entity/class :unit}))


(facts
 "remove unit"

 (remove-unit {:game/phase :setup
               :game/selected :cube-1
               :game/battlefield {:cube-1 {:unit/player 1
                                           :unit/id 1}}
               :game/units {1 {:counter 1 :cubes {1 :cube-1}}}})
 => {:game/phase :setup
     :game/subphase :select-hex
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

 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)]

   (select {:game/phase :movement
            :game/subphase :select-hex
            :game/battlefield battlefield
            :movement/moved? true}
           :cube-1)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield battlefield
       :game/selected :cube-1
       :game/battlemap :battlemap-2
       :movement/selected pointer}

   (provided
    (mlm/show-reform battlefield :cube-1) => :battlemap-1
    (cm/set-mover-selected :battlemap-1 pointer) => :battlemap-2)))


(facts
 "unselect movement"

 (unselect {:game/phase :movement
            :game/selected :cube-1
            :movement/selected :pointer-1
            :game/battlemap :battlemap-1})
 => {:game/phase :movement
     :game/subphase :select-hex})


(facts
 "select movement reform"

 (let [state {:game/phase :movement
              :game/subphase :reform
              :movement/selected {:cube :cube-1}}]

   (select state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))


 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)]

   (select {:game/phase :movement
            :game/subphase :reform
            :game/battlefield battlefield
            :movement/moved? true}
           :cube-1)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield battlefield
       :game/selected :cube-1
       :game/battlemap :battlemap-2
       :movement/selected pointer}

   (provided
    (mlm/show-reform battlefield :cube-1) => :battlemap-1
    (cm/set-mover-selected :battlemap-1 pointer) => :battlemap-2)))


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

 (let [pointer (mc/->Pointer :cube-1 :s)]

   (move {:game/phase :movement
          :game/subphase :reform
          :game/battlefield {:cube-1 {:unit/facing :n}}
          :game/battlemap :battlemap-1}
         pointer)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield {:cube-1 {:unit/facing :n}}
       :game/battlemap :battlemap-2
       :movement/moved? true
       :movement/selected pointer}

   (provided
    (cm/set-mover-selected :battlemap-1 pointer) => :battlemap-2))


 (let [pointer (mc/->Pointer :cube-1 :n)]

   (move {:game/phase :movement
          :game/subphase :reform
          :game/battlefield {:cube-1 {:unit/facing :n}}
          :game/battlemap :battlemap-1
          :movement/moved? true}
         pointer)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield {:cube-1 {:unit/facing :n}}
       :game/battlemap :battlemap-2
       :movement/selected pointer}

   (provided
    (cm/set-mover-selected :battlemap-1 pointer) => :battlemap-2)))


(facts
 "move movement forward"

 (let [pointer (mc/->Pointer :cube-1 :n)
       battlefield {:cube-1 {:entity/cube :cube-1
                             :unit/facing :n}}
       battlemap {:cube-1 :mover-1
                  :cube-2 :mover-2}
       breadcrumbs {pointer {:cube-1 :mover-3}}]

   (move {:game/phase :movement
          :game/subphase :forward
          :game/battlefield battlefield
          :movement/battlemap battlemap
          :movement/breadcrumbs breadcrumbs
          :movement/moved? true}
         pointer)
   => {:game/phase :movement
       :game/subphase :forward
       :game/battlefield battlefield
       :game/battlemap :battlemap-2
       :movement/battlemap battlemap
       :movement/breadcrumbs breadcrumbs
       :movement/selected pointer}

   (provided
    (cm/set-mover-selected {:cube-1 :mover-3
                            :cube-2 :mover-2}
                           pointer)
    => :battlemap-2))


 (let [pointer (mc/->Pointer :cube-1 :n)
       battlefield {:cube-1 {:entity/cube :cube-1
                             :unit/facing :s}}
       battlemap {:cube-1 :mover-1
                  :cube-2 :mover-2}
       breadcrumbs {pointer {:cube-1 :mover-3}}]

   (move {:game/phase :movement
          :game/subphase :forward
          :game/battlefield battlefield
          :movement/battlemap battlemap
          :movement/breadcrumbs breadcrumbs}
         pointer)
   => {:game/phase :movement
       :game/subphase :forward
       :game/battlefield battlefield
       :game/battlemap :battlemap-2
       :movement/battlemap battlemap
       :movement/breadcrumbs breadcrumbs
       :movement/moved? true
       :movement/selected pointer}

   (provided
    (cm/set-mover-selected {:cube-1 :mover-3
                            :cube-2 :mover-2}
                           pointer)
    => :battlemap-2)))


(facts
 "finish movement"

 (let [pointer (mc/->Pointer :cube-2 :n)
       unit {:unit/player 1
             :unit/id 2
             :entity/presentation :selected}]

   (finish-movement {:game/selected :cube-1
                     :game/battlefield {:cube-1 unit}
                     :movement/selected pointer
                     :movement/moved? true})
   => {:game/selected :cube-1
       :game/battlefield {:cube-1 :terrain
                          :cube-2 {:unit/player 1
                                   :unit/id 2
                                   :entity/cube :cube-2
                                   :unit/facing :n
                                   :entity/presentation :default}}
       :game/units {1 {:cubes {2 :cube-2}}}
       :movement/selected pointer
       :movement/moved? true
       :game/subphase :select-hex}

   (provided
    (me/gen-terrain :cube-1) => :terrain
    (ce/reset-default unit) => (assoc unit :entity/presentation :default))))


(facts
 "movement reform"

 (movement-reform {:movement/selected :pointer-1
                   :game/selected :cube-1})
 => [:select :reform]

 (provided
  (select {:game/subphase :reform
           :game/selected :cube-1}
          :cube-1)
  => [:select :reform]))


(facts
 "movement forward"

 (movement-forward {:movement/selected :pointer-1
                    :game/selected :cube-1})
 => [:select :forward]

 (provided
  (select {:game/subphase :forward
           :game/selected :cube-1}
          :cube-1)
  => [:select :forward]))


(facts
 "select movement move"

 (let [state {:game/phase :movement
              :game/subphase :forward
              :movement/selected {:cube :cube-1}}]

   (select state :cube-1)
   => :unselected

   (provided
    (unselect state) => :unselected))

 (let [battlefield {:cube-1 {:unit/facing :n}}

       state {:game/phase :movement
              :game/subphase :forward
              :game/battlefield battlefield
              :movement/moved? true}

       pointer (mc/->Pointer :cube-1 :n)]

   (select state :cube-1)

   => {:game/phase :movement
       :game/subphase :forward
       :game/battlefield battlefield
       :game/selected :cube-1
       :game/battlemap :battlemap-2
       :movement/battlemap :battlemap-1
       :movement/breadcrumbs :breadcrumbs-1
       :movement/selected pointer}

   (provided
    (mlm/show-forward battlefield :cube-1)
    => {:battlemap :battlemap-1
        :breadcrumbs :breadcrumbs-1}

    (cm/set-mover-selected :battlemap-1 pointer)
    => :battlemap-2)))
