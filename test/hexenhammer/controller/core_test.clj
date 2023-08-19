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
           :facing-1
           :M 3)
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/class :unit
                                 :entity/presentation :default}}
     :game/units {1 {:counter 1 :cubes {1 :cube-1}}}}

 (provided
  (me/gen-unit :cube-1 1 1 :facing-1 :interaction :selectable :M 3)
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

 (let [state {:game/phase :movement
              :game/subphase :forward}]

   (move state :pointer)
   => :show-moves

   (provided
    (cm/show-moves state :pointer)
    => :show-moves)))


(facts
 "move movement forward"

 (let [state {:game/phase :movement
              :game/subphase :forward}]

   (move state :pointer)
   => :show-moves

   (provided
    (cm/show-moves state :pointer)
    => :show-moves)))

(facts
 "move movement reposition"

 (let [state {:game/phase :movement
              :game/subphase :reposition}]

   (move state :pointer)
   => :show-moves

   (provided
    (cm/show-moves state :pointer)
    => :show-moves)))

(facts
 "move movement march"

 (let [state {:game/phase :movement
              :game/subphase :march}]

   (move state :pointer)
   => :show-moves

   (provided
    (cm/show-moves state :pointer)
    => :show-moves)))


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
 "movement transition"

 (movement-transition {:movement/selected :pointer-1
                       :game/selected :cube-1}
                      :movement-1)
 => [:select :movement-1]

 (provided
  (select {:game/subphase :movement-1
           :game/selected :cube-1}
          :cube-1)
  => [:select :movement-1]))


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
       pointer (mc/->Pointer :cube-1 :n)
       state {:game/phase :movement
              :game/subphase :reform
              :game/battlefield battlefield}]

   (select state :cube-1)
   => :move

   (provided
    (mlm/show-reform battlefield :cube-1)
    => :battlemap

    (move {:game/phase :movement
           :game/subphase :reform
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap}
          pointer)
    => :move)))
