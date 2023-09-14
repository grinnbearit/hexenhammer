(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.unit :as mu]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.core :as l]
            [hexenhammer.logic.unit :as lu]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.movement :as lm]
            [hexenhammer.controller.dice :as cd]
            [hexenhammer.controller.unit :as cu]
            [hexenhammer.controller.event :as ce]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.battlemap :as cb]
            [hexenhammer.controller.core :refer :all]))


(facts
 "select setup select-hex"

 (select {:game/phase :setup
          :game/subphase :select-hex
          :game/battlefield {:cube-1 {:entity/class :terrain}}}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :add-unit
     :game/selected :cube-1
     :game/battlefield {:cube-1 {:entity/class :terrain
                                 :entity/state :selected}}}

 (select {:game/phase :setup
          :game/subphase :select-hex
          :game/battlefield {:cube-1 {:entity/class :unit}}}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :remove-unit
     :game/selected :cube-1
     :game/battlefield {:cube-1 {:entity/class :unit
                                 :entity/state :selected}}})


(facts
 "unselect setup"

 (unselect {:game/phase :setup
            :game/selected :cube-1
            :game/battlefield {:cube-1 {:entity/state :selected}}})
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/state :silent-selectable}}})


(facts
 "select setup add unit"

 (select {:game/phase :setup
          :game/subphase :add-unit
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:entity/state :selected}}}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/state :silent-selectable}}}


 (select {:game/phase :setup
          :game/subphase :add-unit
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:entity/state :selected}
                             :cube-2 {:entity/class :terrain}}}
         :cube-2)
 => {:game/phase :setup
     :game/subphase :add-unit
     :game/selected :cube-2
     :game/battlefield {:cube-1 {:entity/state :silent-selectable}
                        :cube-2 {:entity/class :terrain
                                 :entity/state :selected}}})


(facts
 "select setup remove unit"

 (select {:game/phase :setup
          :game/subphase :remove-unit
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:entity/state :selected}}}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/battlefield {:cube-1 {:entity/state :silent-selectable}}}


 (select {:game/phase :setup
          :game/subphase :remove-unit
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:entity/state :selected}
                             :cube-2 {:entity/class :terrain}}}
         :cube-2)
 => {:game/phase :setup
     :game/subphase :add-unit
     :game/selected :cube-2
     :game/battlefield {:cube-1 {:entity/state :silent-selectable}
                        :cube-2 {:entity/class :terrain
                                 :entity/state :selected}}})


(facts
 "add unit"

 (add-unit {:game/selected :cube-1
            :game/battlefield {:cube-1 :terrain-1}}
           1
           :facing-1
           {:M 3 :Ld 7})
 => :unselect

 (provided
  (me/gen-infantry :cube-1 1 1 :facing-1 :M 3 :Ld 7)
  => {:entity/class :unit}

  (lt/place {:entity/class :unit
             :entity/state :selectable}
            :terrain-1)
  => :place

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :place}
             :game/units {1 {"infantry" {:counter 1 :cubes {1 :cube-1}}}}})
  => :unselect))


(facts
 "remove unit"

 (let [unit {:unit/player 1
             :entity/name "unit"
             :unit/id 1}]

   (remove-unit {:game/selected :cube-1
                 :game/battlefield {:cube-1 unit}
                 :game/units {1 {"unit" {:counter 1 :cubes {1 :cube-1}}}}})
   => :unselect

   (provided

    (lt/pickup unit) => {:entity/class :terrain}

    (unselect {:game/selected :cube-1
               :game/battlefield {:cube-1 {:entity/class :terrain
                                           :entity/state :selectable}}
               :game/units {1 {"unit" {:counter 1 :cubes {}}}}})
    => :unselect)))


(facts
 "swap terrain"

 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :open)
 => :unselect

 (provided
  (me/gen-open-ground :cube-1) => :open-terrain

  (le/terrain? :entity-1) => true

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :open-terrain}})
  => :unselect)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :dangerous)
 => :unselect

 (provided
  (me/gen-dangerous-terrain :cube-1) => :dangerous-terrain

  (le/terrain? :entity-1) => true

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :dangerous-terrain}})
  => :unselect)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :impassable)
 => :unselect

 (provided
  (me/gen-impassable-terrain :cube-1) => :impassable-terrain

  (le/terrain? :entity-1) => true

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :impassable-terrain}})
  => :unselect)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :open)
 => :unselect

 (provided
  (me/gen-open-ground :cube-1) => :open-terrain

  (le/terrain? :entity-1) => false

  (lt/place :entity-1 :open-terrain) => :entity-2

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :entity-2}})
  => :unselect))


(facts
 "trigger"

 (trigger {:game/events []})
 => {}

 (provided
  (ce/pop-phase {:game/events []})
  => {:game/battlemap :battlemap-1})


 (let [state {:game/events [:event-2 :event-1]
              :game/battlefield :battlefield-1}]


   (trigger state)
   => :trigger-event

   (provided
    (ce/push-phase state)
    => state

    (l/set-state :battlefield-1 :default) => :battlefield-2

    (ce/event-transition {:game/events [:event-2]
                          :game/battlefield :battlefield-1
                          :game/battlemap :battlefield-2}
                         :event-1)
    => :event-transition

    (trigger-event :event-transition :event-1)
    => :trigger-event)))


(facts
 "trigger event dangerous"

 (let [state {:game/phase :dangerous
              :game/units 1 :cubes {}}

       unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2}]

   (trigger-event state unit)
   => :trigger

   (provided
    (trigger state) => :trigger))


 (let [unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2}

       state {:game/phase :dangerous
              :game/units {1 {"unit" {:cubes {2 :cube-1}}}}
              :game/battlefield {:cube-1 unit}}]

   (trigger-event state {:unit/player 1
                         :entity/name "unit"
                         :unit/id 2})
   => {:game/battlemap :set-state}

   (provided
    (mu/models unit) => 3
    (cd/roll! 3) => :roll
    (cd/matches :roll 1) => 3

    (cu/destroy-unit state unit) => {}

    (cb/refresh-battlemap {:game/trigger {:models-destroyed 3
                                          :unit-destroyed? true
                                          :roll :roll
                                          :unit unit}}
                          [:cube-1])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1] :marked) => :set-state))


 (let [unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2}

       state {:game/phase :dangerous
              :game/units {1 {"unit" {:cubes {2 :cube-1}}}}
              :game/battlefield {:cube-1 unit}}]

   (trigger-event state {:event/cube :cube-2
                         :unit/player 1
                         :entity/name "unit"
                         :unit/id 2})
   => {:game/battlemap :set-state}

   (provided
    (mu/models unit) => 4
    (cd/roll! 4) => :roll
    (cd/matches :roll 1) => 3

    (cu/destroy-models state :cube-2 unit 3) => {}

    (cb/refresh-battlemap {:game/trigger {:models-destroyed 3
                                          :unit-destroyed? false
                                          :roll :roll
                                          :unit unit}}
                          [:cube-1])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1] :marked) => :set-state)))


(facts
 "trigger event panic"

 (let [unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2}

       state {:game/phase :panic
              :game/units {1 {"unit" {:cubes {}}}}}]

   (trigger-event state unit)
   => :trigger

   (provided
    (trigger state) => :trigger))


 (let [unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2}

       state {:game/phase :panic
              :game/battlefield :battlefield
              :game/units {1 {"unit" {:cubes {2 :cube-1}}}}}]

   (trigger-event state unit)
   => :trigger

   (provided
    (lu/panickable? :battlefield :cube-1) => false
    (trigger state) => :trigger))


 (let [unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2
             :unit/Ld 3}

       battlefield {:cube-1 unit}

       state {:game/phase :panic
              :game/units {1 {"unit" {:cubes {2 :cube-1}}}}
              :game/battlefield battlefield}]

   (trigger-event state {:event/cube :cube-2
                         :unit/player 1
                         :entity/name "unit"
                         :unit/id 2})
   => {:game/phase :panic
       :game/units {1 {"unit" {:cubes {2 :cube-1}}}}
       :game/battlefield {:cube-1 {:unit/player 1
                                   :entity/name "unit"
                                   :unit/id 2
                                   :unit/Ld 3
                                   :unit/flags {:panicked? true}}}
       :game/subphase :passed
       :game/trigger {:cube :cube-2
                      :unit unit
                      :roll [1 1]}}

   (provided
    (lu/panickable? battlefield :cube-1) => true
    (cd/roll! 2) => [1 1]))


 (let [unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2
             :unit/Ld 3}

       battlefield {:cube-1 unit}

       state {:game/phase :panic
              :game/units {1 {"unit" {:cubes {2 :cube-1}}}}
              :game/battlefield battlefield}]

   (trigger-event state {:event/cube :cube-2
                         :unit/player 1
                         :entity/name "unit"
                         :unit/id 2})
   => {:game/phase :panic
       :game/units {1 {"unit" {:cubes {2 :cube-1}}}}
       :game/battlefield {:cube-1 {:unit/player 1
                                   :entity/name "unit"
                                   :unit/id 2
                                   :unit/Ld 3
                                   :unit/flags {:panicked? true}}}
       :game/subphase :failed
       :game/trigger {:cube :cube-2
                      :unit unit
                      :roll [1 3]}}

   (provided
    (lu/panickable? battlefield :cube-1) => true
    (cd/roll! 2) => [1 3])))


(facts
 "to charge"

 (let [state {:game/player 1
              :game/battlefield :battlefield-1
              :game/units :units-1}]

   (to-charge state)
   => {:game/player 1
       :game/phase :charge
       :game/subphase :select-hex
       :game/battlefield :battlefield-4
       :game/units :units-1}

   (provided
    (cu/unit-cubes state) => [:cube-1 :cube-2 :cube-3]
    (cu/unit-cubes state 1) => [:cube-1 :cube-2]
    (lu/phase-reset :battlefield-1 [:cube-1 :cube-2 :cube-3]) => :battlefield-2
    (lm/charger? :battlefield-1 :cube-1) => true
    (lm/charger? :battlefield-1 :cube-2) => false
    (l/set-state :battlefield-2 :default) => :battlefield-3
    (l/set-state :battlefield-3 [:cube-1] :selectable) => :battlefield-4)))


(facts
 "unselect charge"

 (unselect {:game/phase :charge
            :game/selected :cube-1
            :game/battlemap :battlemap-1})

 => {:game/phase :charge
     :game/subphase :select-hex})


(facts
 "select charge select-hex"

 (select {:game/selected :cube-1
          :game/phase :charge
          :game/subphase :select-hex}
         :cube-1)
 => :unselect

 (provided
  (unselect {:game/selected :cube-1
             :game/phase :charge
             :game/subphase :select-hex})
  => :unselect)


 (select {:game/charge {:pointer {:cube :cube-1}}
          :game/phase :charge
          :game/subphase :select-hex}
         :cube-1)
 => :unselect

 (provided
  (unselect {:game/charge {:pointer {:cube :cube-1}}
             :game/phase :charge
             :game/subphase :select-hex})
  => :unselect)


 (select {:game/selected :cube-2
          :game/phase :charge
          :game/subphase :select-hex
          :game/battlefield :battlefield-1}
         :cube-1)
 => {:game/selected :cube-1
     :game/phase :charge
     :game/subphase :select-target
     :game/battlefield :battlefield-1
     :game/battlemap :battlemap-1
     :game/charge {:battlemap :battlemap-1
                   :breadcrumbs :breadcrumbs-1
                   :pointer->events :pointer->events-1
                   :ranges :ranges-1}}

 (provided
  (lm/show-charge :battlefield-1 :cube-1) => {:battlemap :battlemap-1
                                              :breadcrumbs :breadcrumbs-1
                                              :pointer->events :pointer->events-1
                                              :ranges :ranges-1}))


(facts
 "select charge select-target"

 (select {:game/selected :cube-1
          :game/phase :charge
          :game/subphase :select-target}
         :cube-1)
 => :unselect

 (provided
  (unselect {:game/selected :cube-1
             :game/phase :charge
             :game/subphase :select-hex})
  => :unselect))


(facts
 "select charge declare"

 (select {:game/selected :cube-1
          :game/phase :charge
          :game/subphase :declare}
         :cube-1)
 => :unselect

 (provided
  (unselect {:game/selected :cube-1
             :game/phase :charge
             :game/subphase :select-hex})
  => :unselect))


(facts
 "to movement"

 (let [state {:game/player 1
              :game/battlefield :battlefield-1
              :game/units :units-1}]

   (to-movement state)
   => {:game/player 1
       :game/units :units-1
       :game/phase :movement
       :game/subphase :select-hex
       :game/battlefield :battlefield-3}

   (provided
    (cu/unit-cubes state 1) => [:unit-cube-1 :unit-cube-2]

    (lu/battlefield-engaged? :battlefield-1 :unit-cube-1)
    => false

    (lu/battlefield-engaged? :battlefield-1 :unit-cube-2)
    => true

    (l/set-state :battlefield-1 :default)
    => :battlefield-2

    (l/set-state :battlefield-2 [:unit-cube-1] :selectable)
    => :battlefield-3)))


(facts
 "select movement select-hex"

 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)]

   (select {:game/phase :movement
            :game/subphase :select-hex
            :game/battlefield battlefield}
           :cube-1)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield battlefield
       :game/selected :cube-1
       :game/battlemap :battlemap-2
       :game/movement {:pointer pointer}}

   (provided
    (lm/show-reform battlefield :cube-1) => :battlemap-1
    (cm/set-mover-selected :battlemap-1 pointer) => :battlemap-2)))


(facts
 "unselect movement"

 (unselect {:game/phase :movement
            :game/selected :cube-1
            :game/battlemap :battlemap-1
            :game/movement :movement})
 => {:game/phase :movement
     :game/subphase :select-hex})


(facts
 "skip movement"

 (skip-movement {:game/selected :cube-1
                 :game/battlefield {:cube-1 {}}})
 => :unselect

 (provided

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 {:entity/state :default}}})
  => :unselect))


(facts
 "move charge select-target"

 (let [pointer (mc/->Pointer :cube-1 :n)]

   (move {:game/phase :charge
          :game/subphase :select-target
          :game/charge {:battlemap {:cube-1 :battlemap-entry-1}
                        :breadcrumbs {pointer {:cube-2 :breadcrumbs-entry-1}}}}
         pointer)
   => {:game/phase :charge
       :game/subphase :declare
       :game/battlemap :battlemap-2
       :game/charge {:pointer pointer
                     :battlemap {:cube-1 :battlemap-entry-1}
                     :breadcrumbs {pointer {:cube-2 :breadcrumbs-entry-1}}}}

   (provided
    (cm/set-mover-selected {:cube-1 :battlemap-entry-1
                            :cube-2 :breadcrumbs-entry-1}
                           pointer)
    => :battlemap-2)))


(facts
 "move charge declare"

 (let [pointer (mc/->Pointer :cube-1 :n)]

   (move {:game/phase :charge
          :game/subphase :declare
          :game/charge {:battlemap {:cube-1 :battlemap-entry-1}
                        :breadcrumbs {pointer {:cube-2 :breadcrumbs-entry-1}}}}
         pointer)
   => {:game/phase :charge
       :game/subphase :declare
       :game/battlemap :battlemap-2
       :game/charge {:pointer pointer
                     :battlemap {:cube-1 :battlemap-entry-1}
                     :breadcrumbs {pointer {:cube-2 :breadcrumbs-entry-1}}}}

   (provided
    (cm/set-mover-selected {:cube-1 :battlemap-entry-1
                            :cube-2 :breadcrumbs-entry-1}
                           pointer)
    => :battlemap-2)))


(facts
 "move movement reform"

 (let [pointer (mc/->Pointer :cube-1 :n)]

   (move {:game/phase :movement
          :game/subphase :reform
          :game/battlefield {:cube-1 {:unit/facing :n}}
          :game/battlemap :battlemap-1}
         pointer)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield {:cube-1 {:unit/facing :n}}
       :game/battlemap :battlemap-2
       :game/movement {:pointer pointer}}

   (provided
    (cm/set-mover-selected :battlemap-1 pointer) => :battlemap-2))


 (let [pointer (mc/->Pointer :cube-1 :s)]

   (move {:game/phase :movement
          :game/subphase :reform
          :game/battlefield {:cube-1 {:unit/facing :n}}
          :game/battlemap :battlemap-1
          :game/movement {:moved? true
                          :pointer pointer}}
         pointer)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield {:cube-1 {:unit/facing :n}}
       :game/battlemap :battlemap-2
       :game/movement {:moved? true
                       :pointer pointer}}

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

 (let [pointer (mc/->Pointer :cube-1 :n)
       unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2}]

   (finish-movement {:game/selected :cube-1
                     :game/battlefield {:cube-1 unit
                                        :cube-2 :terrain-2}
                     :game/movement {:pointer pointer
                                     :pointer->events {pointer [:event-1 :event-2]}}
                     :game/events []})
   => :trigger

   (provided

    (lt/pickup unit) => :old-terrain

    (lt/swap {:unit/player 1
              :entity/name "unit"
              :unit/id 2
              :entity/state :default
              :entity/cube :cube-1
              :unit/facing :n
              :unit/flags {:marched? false}}
             unit)
    => :unit-2

    (unselect {:game/selected :cube-1
               :game/battlefield {:cube-1 :unit-2
                                  :cube-2 :terrain-2}
               :game/movement {:pointer pointer
                               :pointer->events {pointer [:event-1 :event-2]}}
               :game/units {1 {"unit" {:cubes {2 :cube-1}}}}
               :game/events [:event-1 :event-2]})
    => :unselect

    (trigger :unselect)
    => :trigger))


 (let [pointer (mc/->Pointer :cube-1 :n)
       unit {:unit/player 1
             :entity/name "unit"
             :unit/id 2}]

   (finish-movement {:game/selected :cube-1
                     :game/battlefield {:cube-1 unit
                                        :cube-2 :terrain-2}
                     :game/movement {:pointer pointer
                                     :pointer->events {pointer [:event-1 :event-2]}
                                     :marched? true}
                     :game/events []})
   => :trigger

   (provided

    (lt/pickup unit) => :old-terrain

    (lt/swap {:unit/player 1
              :entity/name "unit"
              :unit/id 2
              :entity/state :default
              :entity/cube :cube-1
              :unit/facing :n
              :unit/flags {:marched? true}}
             unit)
    => :unit-2

    (unselect {:game/selected :cube-1
               :game/battlefield {:cube-1 :unit-2
                                  :cube-2 :terrain-2}
               :game/movement {:pointer pointer
                               :pointer->events {pointer [:event-1 :event-2]}
                               :marched? true}
               :game/units {1 {"unit" {:cubes {2 :cube-1}}}}
               :game/events [:event-1 :event-2]})
    => :unselect

    (trigger :unselect)
    => :trigger)))


(facts
 "movement transition"

 (movement-transition {:game/movement {:pointer :pointer-1}
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
              :game/movement {:pointer {:cube :cube-1}}}]

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
    (lm/show-reform battlefield :cube-1)
    => :battlemap

    (move {:game/phase :movement
           :game/subphase :reform
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap}
          pointer)
    => :move)))


(facts
 "select movement forward"

 (let [state {:game/phase :movement
              :game/subphase :forward
              :game/movement {:pointer {:cube :cube-1}}}]

   (select state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))


 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)
       state {:game/phase :movement
              :game/subphase :forward
              :game/battlefield battlefield}]

   (select state :cube-1)
   => :move

   (provided
    (lm/show-forward battlefield :cube-1)
    => {:battlemap :battlemap-1
        :breadcrumbs :breadcrumbs-1
        :pointer->events :p->e-1}

    (move {:game/phase :movement
           :game/subphase :forward
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :pointer->events :p->e-1}}
          pointer)
    => :move)))


(facts
 "select movement forward"

 (let [state {:game/phase :movement
              :game/subphase :reposition
              :game/movement {:pointer {:cube :cube-1}}}]

   (select state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))


 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)
       state {:game/phase :movement
              :game/subphase :reposition
              :game/battlefield battlefield}]

   (select state :cube-1)
   => :move

   (provided
    (lm/show-reposition battlefield :cube-1)
    => {:battlemap :battlemap-1
        :breadcrumbs :breadcrumbs-1
        :pointer->events :p->e-1}

    (move {:game/phase :movement
           :game/subphase :reposition
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :pointer->events :p->e-1}}
          pointer)
    => :move)))


(facts
 "select movement march"

 (let [state {:game/phase :movement
              :game/subphase :march
              :game/movement {:pointer {:cube :cube-1}}}]

   (select state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))


 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)
       state {:game/phase :movement
              :game/subphase :march
              :game/battlefield battlefield}]

   (select state :cube-1)
   => :move

   (provided
    (lm/show-march battlefield :cube-1)
    => {:battlemap :battlemap-1
        :breadcrumbs :breadcrumbs-1
        :threats? false
        :pointer->events :p->e-1}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :march :unnecessary
                           :pointer->events :p->e-1}}
          pointer)
    => :move))


 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)
       state {:game/phase :movement
              :game/subphase :march
              :game/battlefield battlefield}]

   (select state :cube-1)
   => :move

   (provided
    (lm/show-march battlefield :cube-1)
    => {:battlemap :battlemap-1
        :breadcrumbs :breadcrumbs-1
        :threats? true
        :pointer->events :p->e-1}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :march :required
                           :pointer->events :p->e-1}}
          pointer)
    => :move))


 (let [battlefield {:cube-1 {:unit/facing :n
                             :unit/movement {:marched? true :passed? true}}}
       pointer (mc/->Pointer :cube-1 :n)
       state {:game/phase :movement
              :game/subphase :march
              :game/battlefield battlefield}]

   (select state :cube-1)
   => :move

   (provided
    (lm/show-march battlefield :cube-1)
    => {:battlemap :battlemap-1
        :breadcrumbs :breadcrumbs-1
        :pointer->events :p->e-1
        :threats? true}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :march :passed
                           :pointer->events :p->e-1}}
          pointer)
    => :move))


 (let [battlefield {:cube-1 {:unit/facing :n
                             :unit/movement {:marched? true
                                             :passed? false}}}
       pointer (mc/->Pointer :cube-1 :n)
       state {:game/phase :movement
              :game/subphase :march
              :game/battlefield battlefield}]

   (select state :cube-1)
   => :move

   (provided
    (lm/show-march battlefield :cube-1)
    => {:battlemap :battlemap-1
        :breadcrumbs :breadcrumbs-1
        :pointer->events :p->e-1
        :threats? true}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :march :failed
                           :pointer->events :p->e-1}}
          pointer)
    => :move)))


(facts
 "test march!"

 (test-march! {:game/selected :cube-1
               :game/movement {:pointer :pointer-1}
               :game/battlefield {:cube-1 {:unit/Ld 7}}})
 => :move

 (provided
  (cd/roll! 2) => [2 3]

  (unselect {:game/selected :cube-1
             :game/movement {:pointer :pointer-1}
             :game/battlefield {:cube-1 {:unit/Ld 7
                                         :unit/movement {:marched? true
                                                         :roll [2 3]
                                                         :passed? true}}}})
  => :unselect

  (select :unselect :cube-1)
  => :select

  (movement-transition :select :march)
  => :movement-transition

  (move :movement-transition :pointer-1)
  => :move)


 (test-march! {:game/selected :cube-1
               :game/movement {:pointer :pointer-1}
               :game/battlefield {:cube-1 {:unit/Ld 7}}})
 => :move

 (provided
  (cd/roll! 2) => [5 3]

  (unselect {:game/selected :cube-1
             :game/movement {:pointer :pointer-1}
             :game/battlefield {:cube-1 {:unit/Ld 7
                                         :unit/movement {:marched? true
                                                         :roll [5 3]
                                                         :passed? false}}}})
  => :unselect

  (select :unselect :cube-1)
  => :select

  (movement-transition :select :march)
  => :movement-transition

  (move :movement-transition :pointer-1)
  => :move))
