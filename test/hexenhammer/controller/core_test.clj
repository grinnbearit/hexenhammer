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

 (let [battlefield {:cube-1 {:unit/facing :n}}]

   (select {:game/phase :movement
            :game/subphase :select-hex
            :game/battlefield battlefield
            :movement/moved? true}
           :cube-1)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield battlefield
       :game/selected :cube-1
       :game/battlemap :battlemap-2}

   (provided
    (mlm/show-reform battlefield :cube-1) => :battlemap-1
    (cm/set-mover-selected :battlemap-1 (mc/->Pointer :cube-1 :n)) => :battlemap-2)))


(facts
 "select movement reform"

 (let [battlefield {:cube-1 {:unit/facing :n}}]

   (select {:game/phase :movement
            :game/subphase :reform
            :game/battlefield battlefield
            :movement/moved? true}
           :cube-1)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield battlefield
       :game/selected :cube-1
       :game/battlemap :battlemap-2}

   (provided
    (mlm/show-reform battlefield :cube-1) => :battlemap-1
    (cm/set-mover-selected :battlemap-1 (mc/->Pointer :cube-1 :n)) => :battlemap-2)))


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
       :movement/moved? true}

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
       :game/battlemap :battlemap-2}

   (provided
    (cm/set-mover-selected :battlemap-1 pointer) => :battlemap-2)))


(facts
 "move movement move"

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
       :movement/breadcrumbs breadcrumbs}

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
       :movement/moved? true}

   (provided
    (cm/set-mover-selected {:cube-1 :mover-3
                            :cube-2 :mover-2}
                           pointer)
    => :battlemap-2)))


(facts
 "finish movement"

 (finish-movement {:game/selected :cube-1
                   :game/battlefield {:cube-1 :unit-1}
                   :game/battlemap {:cube-1 {:mover/selected :n}}
                   :movement/moved? true})
 => {:game/battlefield {:cube-1 {:unit/facing :n}}
     :game/subphase :select-hex}

 (provided
  (ce/reset-default :unit-1)
  => {}))


(facts
 "movement reform"

 (movement-reform {:mock :state-1 :game/selected :cube-1})
 => [:select :cube-1]

 (provided
  (cm/reset-state {:mock :state-1 :game/selected :cube-1})
  => {:mock :state-2 :game/selected :cube-1}

  (select {:mock :state-2
           :game/selected :cube-1
           :game/subphase :reform}
          :cube-1)
  => [:select :cube-1]))


(facts
 "movement move"

 (movement-forward {:mock :state-1 :game/selected :cube-1})
 => [:select :cube-1]

 (provided
  (cm/reset-state {:mock :state-1 :game/selected :cube-1})
  => {:mock :state-2 :game/selected :cube-1}

  (select {:mock :state-2
           :game/selected :cube-1
           :game/subphase :forward}
          :cube-1)
  => [:select :cube-1]))


(facts
 "select movement move"

 (let [battlefield {:cube-1 {:unit/facing :n}}

       state {:game/phase :movement
              :game/subphase :forward
              :mock :state-1
              :game/battlefield battlefield
              :movement/moved? true}]

   (select state :cube-1)

   => {:mock :state-2
       :game/phase :movement
       :game/subphase :forward
       :game/selected :cube-1
       :game/battlefield battlefield
       :game/battlemap :battlemap-2
       :movement/battlemap :battlemap-1
       :movement/breadcrumbs :breadcrumbs-1}

   (provided
    (mlm/show-forward battlefield :cube-1)
    => {:battlemap :battlemap-1
        :breadcrumbs :breadcrumbs-1}

    (cm/reset-state state) => (-> (assoc state :mock :state-2)
                                  (dissoc :movement/moved?))

    (cm/set-mover-selected :battlemap-1 (mc/->Pointer :cube-1 :n))
    => :battlemap-2)))
