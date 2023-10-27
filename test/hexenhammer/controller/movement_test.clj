(ns hexenhammer.controller.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement :as lbm]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.controller.movement :refer :all]))


(facts
 "unselect"

 (unselect {:game/movement {:movable-keys [:unit-key-1 :unit-key-2]
                            :movable-cubes [:cube-1 :cube-2]
                            :cube->enders {}}
            :game/cube :cube-1})
 => {:game/battlemap :battlemap-2}

 (provided
  (t/reset-battlemap {:game/movement {:movable-keys [:unit-key-1 :unit-key-2]
                                      :movable-cubes [:cube-1 :cube-2]}
                      :game/phase [:movement :select-hex]}
                     [:cube-1 :cube-2])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selectable)
  => :battlemap-2))


(facts
 "select reform"

 (let [state {:game/cube :cube-1}]

   (select-reform state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))


 (let [state {:game/battlefield :battlefield-1}]

   (select-reform state :cube-1)
   => {:game/battlefield :battlefield-1
       :game/cube :cube-1
       :game/battlemap :battlemap-1
       :game/movement {:cube->enders :cube->enders-1}}

   (provided
    (lbm/reform :battlefield-1 :cube-1) => {:cube->enders :cube->enders-1}

    (tb/set-presentation :cube->enders-1 [:cube-1] :selected)
    => :battlemap-1)))


(facts
 "move reform"

 (let [state {:game/cube :cube-1
              :game/battlemap {:cube-1 {}}}
       pointer (lc/->Pointer :cube-1 :n)]

   (move-reform state pointer)
   => {:game/cube :cube-1
       :game/pointer pointer
       :game/movement {:moved? true}
       :game/battlemap {:cube-1 {:mover/selected :n}}}))


(facts
 "select hex"

 (select-hex {} :cube-1)
 => :select-reform

 (provided
  (select-reform {:game/phase [:movement :reform]} :cube-1)
  => :select-reform))


(facts
 "skip movement"

 (let [state {:game/cube :cube-1
              :game/battlefield :battlefield-1
              :game/movement {:movable-keys #{:unit-key-1 :unit-key-2}
                              :movable-cubes #{:cube-1 :cube-2}}}]

   (skip-movement state)
   => :unselect

   (provided
    (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1

    (unselect {:game/cube :cube-1
               :game/battlefield :battlefield-1
               :game/movement {:movable-keys #{:unit-key-2}
                               :movable-cubes #{:cube-2}}})
    => :unselect)))


(facts
 "finish movement"

 (let [state {:game/cube :cube-1
              :game/pointer :pointer-1
              :game/battlefield :battlefield-1}]

   (finish-movement state)
   => :skip-movement

   (provided
    (lbu/move-unit :battlefield-1 :cube-1 :pointer-1)
    => :battlefield-2

    (skip-movement {:game/cube :cube-1
                    :game/pointer :pointer-1
                    :game/battlefield :battlefield-2})
    => :skip-movement)))
