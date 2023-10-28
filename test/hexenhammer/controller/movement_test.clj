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
 "set movement"

 (let [state {:game/battlefield :battlefield-1}
       cube->enders {:cube-1 :mover-1}
       logic-fn (fn [_ _] {:cube->enders cube->enders})]

   (set-movement state logic-fn :phase-1 :cube-1)
   => {:game/battlefield :battlefield-1
       :game/cube :cube-1
       :game/phase :phase-1
       :game/battlemap :battlemap-2
       :game/movement {:battlemap cube->enders}}

   (provided
    (tb/set-presentation cube->enders [:cube-1] :selected)
    => :battlemap-2))


 (let [battlefield {:cube-1 :unit-1}
       state {:game/battlefield battlefield}
       cube->enders {:cube-2 :mover-2}
       logic-fn (fn [_ _] {:cube->enders cube->enders})
       battlemap {:cube-1 :unit-1
                  :cube-2 :mover-2}]

   (set-movement state logic-fn :phase-1 :cube-1)
   => {:game/battlefield battlefield
       :game/cube :cube-1
       :game/phase :phase-1
       :game/battlemap :battlemap-2
       :game/movement {:battlemap battlemap}}

   (provided
    (tb/set-presentation battlemap [:cube-1] :selected)
    => :battlemap-2)))


(facts
 "move movement"

 (let [battlemap {:cube-1 {:entity/class :mover}}
       state {:game/movement {:battlemap battlemap}}
       pointer (lc/->Pointer :cube-1 :n)]

   (move-forward state pointer)
   => {:game/pointer pointer
       :game/battlemap :battlemap-2
       :game/movement {:moved? true
                       :battlemap battlemap}}

   (provided
    (tb/set-presentation {:cube-1 {:entity/class :mover
                                   :mover/selected :n}}
                         [:cube-1]
                         :selected)
    => :battlemap-2)))


(facts
 "set reform"

 (set-reform :state :cube)
 => :set-movement

 (provided
  (set-movement :state lbm/reform [:movement :reform] :cube) => :set-movement))


(facts
 "select reform"

 (select-reform :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "move reform"

 (move-reform :state :pointer) => :move-movement

 (provided
  (move-movement :state :pointer) => :move-movement))


(facts
 "set forward"

 (set-forward :state :cube)
 => :set-movement

 (provided
  (set-movement :state lbm/forward [:movement :forward] :cube) => :set-movement))


(facts
 "select forward"

 (select-forward :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "move forward"

 (move-forward :state :pointer) => :move-movement

 (provided
  (move-movement :state :pointer) => :move-movement))


(facts
 "set reposition"

 (set-reposition :state :cube)
 => :set-movement

 (provided
  (set-movement :state lbm/reposition [:movement :reposition] :cube) => :set-movement))


(facts
 "select reposition"

 (select-reposition :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "move reposition"

 (move-reposition :state :pointer) => :move-movement

 (provided
  (move-movement :state :pointer) => :move-movement))


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
    (set-forward :unselect :cube-1) => :set-forward))


 (let [state {:game/cube :cube-1}]

   (switch-movement state :reposition)
   => :set-reposition

   (provided
    (unselect state) => :unselect
    (set-reposition :unselect :cube-1) => :set-reposition)))
