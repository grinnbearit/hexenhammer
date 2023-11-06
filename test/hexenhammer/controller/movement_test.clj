(ns hexenhammer.controller.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement :as lbm]
            [hexenhammer.transition.dice :as td]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.units :as tsu]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.event :as ce]
            [hexenhammer.controller.movement :refer :all]))


(facts
 "unselect"

 (unselect {:game/movement {:movable-keys [:unit-key-1 :unit-key-2]
                            :movable-cubes [:cube-1 :cube-2]
                            :cube->enders {}}
            :game/cube :cube-1})
 => {:game/battlemap :battlemap-2}

 (provided
  (tsb/reset-battlemap {:game/movement {:movable-keys [:unit-key-1 :unit-key-2]
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
       logic-fn (fn [_ _] {:cube->enders cube->enders
                           :pointer->cube->tweeners :pointer->cube->tweeners-1
                           :pointer->events :pointer->events-1})]

   (set-movement state logic-fn :phase-1 :cube-1)
   => {:game/battlefield :battlefield-1
       :game/cube :cube-1
       :game/phase :phase-1
       :game/battlemap :battlemap-2
       :game/movement {:battlemap cube->enders
                       :pointer->cube->tweeners :pointer->cube->tweeners-1
                       :pointer->events :pointer->events-1}}

   (provided
    (tb/set-presentation cube->enders [:cube-1] :selected)
    => :battlemap-2))


 (let [battlefield {:cube-1 :unit-1}
       state {:game/battlefield battlefield}
       cube->enders {:cube-2 :mover-2}
       logic-fn (fn [_ _] {:cube->enders cube->enders
                           :pointer->cube->tweeners :pointer->cube->tweeners-1
                           :pointer->events :pointer->events-1})
       battlemap {:cube-1 :unit-1
                  :cube-2 :mover-2}]

   (set-movement state logic-fn :phase-1 :cube-1)
   => {:game/battlefield battlefield
       :game/cube :cube-1
       :game/phase :phase-1
       :game/battlemap :battlemap-2
       :game/movement {:battlemap battlemap
                       :pointer->cube->tweeners :pointer->cube->tweeners-1
                       :pointer->events :pointer->events-1}}

   (provided
    (tb/set-presentation battlemap [:cube-1] :selected)
    => :battlemap-2)))


(facts
 "move movement"

 (let [battlemap {:cube-1 {:entity/class :mover
                           :mover/presentation :future}
                  :cube-2 {:entity/class :mover
                           :mover/presentation :future}}
       pointer (lc/->Pointer :cube-2 :n)
       pointer->cube->tweeners {pointer {:cube-2 {:entity/class :mover
                                                  :mover/presentation :past}
                                         :cube-3 {:entity/class :mover
                                                  :mover/presentation :past}}}
       pointer->events {pointer [:event-1]}
       state {:game/movement {:battlemap battlemap
                              :pointer->cube->tweeners pointer->cube->tweeners
                              :pointer->events pointer->events}}]

   (move-forward state pointer)
   => {:game/pointer pointer
       :game/battlemap {:cube-1 {:entity/class :mover
                                 :mover/presentation :future}
                        :cube-2 {:entity/class :mover
                                 :entity/presentation :selected
                                 :mover/presentation :present
                                 :mover/selected :n}
                        :cube-3 {:entity/class :mover
                                 :mover/presentation :past}}
       :game/movement {:moved? true
                       :battlemap battlemap
                       :pointer->cube->tweeners pointer->cube->tweeners
                       :pointer->events pointer->events
                       :events [:event-1]}}))


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
 "set march"

 (let [state {:game/battlefield :battlefield-1}]

   (set-march state :cube-1)
   => {:game/movement {:march :unnecessary}}

   (provided
    (lbm/list-threats :battlefield-1 :cube-1) => []
    (set-movement state lbm/march [:movement :march] :cube-1) => {}))


 (let [battlefield {:cube-1 :unit-1}
       state {:game/battlefield battlefield}]

   (set-march state :cube-1)
   => {:game/battlemap :battlemap-2}

   (provided
    (lbm/list-threats battlefield :cube-1) => [:cube-2]
    (set-movement state lbm/march [:movement :march] :cube-1) => {}

    (tsb/refresh-battlemap {:game/movement {:march :required
                                            :threats [:cube-2]}}
                           [:cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-2] :marked)
    => :battlemap-2))


 (let [battlefield {:cube-1 {:unit/flags {:marched? true}
                             :unit/state {:movement {:roll :roll-1
                                                     :passed? false}}}}
       state {:game/battlefield battlefield}]

   (set-march state :cube-1)
   => {:game/battlemap :battlemap-2}

   (provided
    (lbm/list-threats battlefield :cube-1) => [:cube-2]
    (set-movement state lbm/march [:movement :march] :cube-1) => {}

    (tsb/refresh-battlemap {:game/movement {:march :failed
                                            :roll :roll-1
                                            :threats [:cube-2]}}
                           [:cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-2] :marked)
    => :battlemap-2))


 (let [battlefield {:cube-1 {:unit/flags {:marched? true}
                             :unit/state {:movement {:roll :roll-1
                                                     :passed? true}}}}
       state {:game/battlefield battlefield}]

   (set-march state :cube-1)
   => {:game/battlemap :battlemap-2}

   (provided
    (lbm/list-threats battlefield :cube-1) => [:cube-2]
    (set-movement state lbm/march [:movement :march] :cube-1) => {}

    (tsb/refresh-battlemap {:game/movement {:march :passed
                                            :roll :roll-1
                                            :threats [:cube-2]}}
                           [:cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-2] :marked)
    => :battlemap-2)))


(facts
 "select march"

 (select-march :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "move march"

 (let [state {:game/movement {:threats [:cube-2]}}]

   (move-march state :pointer)
   => {:game/battlemap :battlemap-2}

   (provided
    (move-movement state :pointer)
    => :state-2

    (tsb/refresh-battlemap :state-2 [:cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-2] :marked)
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
 "reset movement"

 (let [battlefield {:cube-1 :unit-1
                    :cube-2 :unit-2
                    :cube-3 :unit-3}
       state {:game/battlefield battlefield
              :game/units :units-1
              :game/movement {:movable-keys [:unit-key-1 :unit-key-2 :unit-key-3]}}]

   (reset-movement state)
   => :unselect

   (provided
    (tu/get-unit :units-1 :unit-key-1) => :cube-1
    (tu/get-unit :units-1 :unit-key-2) => :cube-2
    (tu/get-unit :units-1 :unit-key-3) => nil

    (leu/fleeing? :unit-1) => false
    (leu/fleeing? :unit-2) => true

    (unselect {:game/battlefield battlefield
               :game/units :units-1
               :game/movement {:movable-keys #{:unit-key-1}
                               :movable-cubes #{:cube-1}}})
    => :unselect)))


(facts
 "finish movement"

 (let [pointer->events {:pointer-1 [:event-1 :event-2]}
       movement {:movable-keys #{:unit-key-1 :unit-key-2}
                 :movable-cubes #{:cube-1 :cube-2}
                 :pointer->events pointer->events}
       state {:game/cube :cube-1
              :game/pointer :pointer-1
              :game/battlefield :battlefield-1
              :game/movement movement
              :game/events []}]

   (finish-movement state)
   => :trigger

   (provided
    (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1

    (tsu/move-unit {:game/cube :cube-1
                    :game/pointer :pointer-1
                    :game/battlefield :battlefield-1
                    :game/movement {:movable-keys #{:unit-key-2}
                                    :movable-cubes #{:cube-2}
                                    :pointer->events pointer->events}
                    :game/events [:event-1 :event-2]}
                   :cube-1
                   :pointer-1)
    => :move-unit

    (unselect :move-unit)
    => :unselect

    (ce/trigger :unselect reset-movement)
    => :trigger)))


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
    (set-reposition :unselect :cube-1) => :set-reposition))


 (let [state {:game/cube :cube-1}]

   (switch-movement state :march)
   => :set-march

   (provided
    (unselect state) => :unselect
    (set-march :unselect :cube-1) => :set-march)))


(facts
 "test leadership"

 (test-leadership {:game/cube :cube-1
                   :game/pointer :pointer-1
                   :game/battlefield {:cube-1 {:unit/Ld 7}}})
 => :move-march

 (provided
  (td/roll! 2) => [2 3]

  (set-march {:game/cube :cube-1
              :game/pointer :pointer-1
              :game/battlefield {:cube-1 {:unit/Ld 7
                                          :unit/flags {:marched? true}
                                          :unit/state {:movement {:roll [2 3]
                                                                  :passed? true}}}}}
             :cube-1)
  => :set-march

  (move-march :set-march :pointer-1)
  => :move-march))
