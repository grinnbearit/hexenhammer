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
 "set reform"

 (let [state {:game/battlefield :battlefield-1}]

   (set-reform state :cube-1)
   => {:game/battlefield :battlefield-1
       :game/cube :cube-1
       :game/phase [:movement :reform]
       :game/battlemap :battlemap-1}

   (provided
    (lbm/reform :battlefield-1 :cube-1) => {:cube->enders :cube->enders-1}

    (tb/set-presentation :cube->enders-1 [:cube-1] :selected)
    => :battlemap-1)))


(facts
 "select reform"

 (select-reform :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


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
 "set forward"

 (let [state {:game/battlefield :battlefield-1}]

   (set-forward state :cube-1)
   => {:game/battlefield :battlefield-1
       :game/cube :cube-1
       :game/phase [:movement :forward]
       :game/battlemap :battlemap-1
       :game/movement {:cube->enders :cube->enders-1}}

   (provided
    (lbm/forward :battlefield-1 :cube-1) => {:cube->enders :cube->enders-1}

    (tb/set-presentation :cube->enders-1 [:cube-1] :selected)
    => :battlemap-1)))


(facts
 "select forward"

 (select-forward :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "move forward"

 (let [cube->enders {:cube-1 {:entity/class :mover}}
       state {:game/movement {:cube->enders cube->enders}}
       pointer (lc/->Pointer :cube-1 :n)]

   (move-forward state pointer)
   => {:game/pointer pointer
       :game/battlemap :battlemap-2
       :game/movement {:moved? true
                       :cube->enders cube->enders}}

   (provided
    (tb/set-presentation {:cube-1 {:entity/class :mover
                                   :mover/selected :n}}
                         [:cube-1]
                         :selected)
    => :battlemap-2)))


(facts
 "select hex"

 (select-hex :state-1 :cube-1)
 => :set-reform

 (provided
  (set-reform :state-1 :cube-1)
  => :set-reform))


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


(facts
 "switch movement"

 (let [state {:game/cube :cube-1}]

   (switch-movement state :reform)
   => :set-reform

   (provided
    (unselect state) => :unselect
    (set-reform :unselect :cube-1) => :set-reform))


 (let [state {:game/cube :cube-1}]

   (switch-movement state :forward)
   => :set-forward

   (provided
    (unselect state) => :unselect
    (set-forward :unselect :cube-1) => :set-forward)))
