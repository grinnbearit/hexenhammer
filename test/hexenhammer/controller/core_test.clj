(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.logic.core :as l]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.logic.movement :as lm]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.dice :as cd]
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
            :game/battlefield {:cube-1 :terrain-1}
            :game/units {1 {:counter 0 :cubes {}}}}
           1
           :facing-1
           {:M 3 :Ld 7})
 => :unselect

 (provided
  (me/gen-unit :cube-1 1 1 :facing-1 :M 3 :Ld 7)
  => {:entity/class :unit}

  (lt/place {:entity/class :unit
             :entity/state :selectable}
            :terrain-1)
  => :place

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :place}
             :game/units {1 {:counter 1 :cubes {1 :cube-1}}}})
  => :unselect))


(facts
 "remove unit"

 (let [unit {:unit/player 1
             :unit/id 1}]

   (remove-unit {:game/selected :cube-1
                 :game/battlefield {:cube-1 unit}
                 :game/units {1 {:counter 1 :cubes {1 :cube-1}}}})
   => :unselect

   (provided

    (lt/pickup unit) => {:entity/class :terrain}

    (unselect {:game/selected :cube-1
               :game/battlefield {:cube-1 {:entity/class :terrain
                                           :entity/state :selectable}}
               :game/units {1 {:counter 1 :cubes {}}}})
    => :unselect)))


(facts
 "swap terrain"

 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :open)
 => :select

 (provided
  (me/gen-open-ground :cube-1) => :open-terrain

  (le/terrain? :entity-1) => true

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :open-terrain}})
  => :unselect

  (select :unselect :cube-1)
  => :select)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :dangerous)
 => :select

 (provided
  (me/gen-dangerous-terrain :cube-1) => :dangerous-terrain

  (le/terrain? :entity-1) => true

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :dangerous-terrain}})
  => :unselect

  (select :unselect :cube-1)
  => :select)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :impassable)
 => :select

 (provided
  (me/gen-impassable-terrain :cube-1) => :impassable-terrain

  (le/terrain? :entity-1) => true

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :impassable-terrain}})
  => :unselect

  (select :unselect :cube-1)
  => :select)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :open)
 => :select

 (provided
  (me/gen-open-ground :cube-1) => :open-terrain

  (le/terrain? :entity-1) => false

  (lt/place :entity-1 :open-terrain) => :entity-2

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :entity-2}})
  => :unselect

  (select :unselect :cube-1)
  => :select))


(facts
 "to charge"

 (to-charge {:game/player 1
             :game/battlefield :battlefield-1
             :game/units {1 {:cubes {:id-1 :cube-1
                                     :id-2 :cube-2}}}})
 => {:game/player 1
     :game/phase :charge
     :game/subphase :select-hex
     :game/battlefield :battlefield-3
     :game/units {1 {:cubes {:id-1 :cube-1
                             :id-2 :cube-2}}}}

 (provided
  (lm/charger? :battlefield-1 :cube-1) => true
  (lm/charger? :battlefield-1 :cube-2) => false
  (l/set-state :battlefield-1 :default) => :battlefield-2
  (l/set-state :battlefield-2 [:cube-1] :selectable) => :battlefield-3))


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
     :game/subphase :select-hex
     :game/battlefield :battlefield-1
     :game/battlemap {:cube-1 :unit-entry-1
                      :cube-3 :battlemap-entry-1}
     :game/charge {:battlemap {:cube-1 :unit-entry-1
                               :cube-3 :battlemap-entry-1}
                   :breadcrumbs :breadcrumbs-1}}

 (provided
  (lm/show-charge :battlefield-1 :cube-1) => {:battlemap {:cube-3 :battlemap-entry-1}
                                              :breadcrumbs :breadcrumbs-1}

  (l/show-cubes :battlefield-1 [:cube-1] :selected) => {:cube-1 :unit-entry-1}))


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
  (l/battlefield-engaged? :battlefield-1 :unit-cube-1)
  => false

  (l/battlefield-engaged? :battlefield-1 :unit-cube-2)
  => true

  (l/set-state :battlefield-1 :default)
  => :battlefield-2

  (l/set-state :battlefield-2 [:unit-cube-1] :selectable)
  => :battlefield-3))


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
 "move charge select-hex"

 (let [pointer (mc/->Pointer :cube-1 :n)]

   (move {:game/phase :charge
          :game/subphase :select-hex
          :game/charge {:battlemap {:cube-1 :battlemap-entry-1}
                        :breadcrumbs {pointer {:cube-2 :breadcrumbs-entry-1}}}}
         pointer)
   => {:game/phase :charge
       :game/subphase :select-hex
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
             :unit/id 2}]

   (finish-movement {:game/selected :cube-1
                     :game/battlefield {:cube-1 unit
                                        :cube-2 :terrain-2}
                     :game/movement {:pointer pointer}})
   => :unselect

   (provided

    (lt/pickup unit) => :old-terrain

    (lt/swap {:unit/player 1
              :unit/id 2
              :entity/state :default
              :entity/cube :cube-1
              :unit/facing :n}
             unit)
    => :unit-2

    (unselect {:game/selected :cube-1
               :game/battlefield {:cube-1 :unit-2
                                  :cube-2 :terrain-2}
               :game/movement {:pointer pointer}
               :game/units {1 {:cubes {2 :cube-1}}}})
    => :unselect))


 (let [pointer (mc/->Pointer :cube-2 :n)
       unit {:unit/player 1
             :unit/id 2}]

   (finish-movement {:game/selected :cube-1
                     :game/battlefield {:cube-1 unit
                                        :cube-2 :terrain-2}
                     :game/movement {:pointer pointer}})
   => :unselect

   (provided

    (lt/pickup unit) => :old-terrain

    (lt/swap {:unit/player 1
              :unit/id 2
              :entity/state :default
              :entity/cube :cube-2
              :unit/facing :n}
             :terrain-2)
    => :unit-2

    (unselect {:game/selected :cube-1
               :game/battlefield {:cube-1 :old-terrain
                                  :cube-2 :unit-2}
               :game/movement {:pointer pointer}
               :game/units {1 {:cubes {2 :cube-2}}}})
    => :unselect)))


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
        :breadcrumbs :breadcrumbs-1}

    (move {:game/phase :movement
           :game/subphase :forward
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1}}
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
        :breadcrumbs :breadcrumbs-1}

    (move {:game/phase :movement
           :game/subphase :reposition
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1}}
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
        :threats? false}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :march :unnecessary}}
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
        :threats? true}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :march :required}}
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
        :threats? true}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :march :passed}}
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
        :threats? true}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :breadcrumbs :breadcrumbs-1
                           :march :failed}}
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
