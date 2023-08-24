(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.logic.entity :as mle]
            [hexenhammer.model.logic.movement :as mlm]
            [hexenhammer.controller.entity :as ce]
            [hexenhammer.controller.battlefield :as cb]
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

  (mle/onto-terrain {:entity/class :unit
                     :entity/state :selectable}
                    :terrain-1)
  => :onto-terrain

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :onto-terrain}
             :game/units {1 {:counter 1 :cubes {1 :cube-1}}}})
  => :unselect))


(facts
 "remove unit"

 (remove-unit {:game/selected :cube-1
               :game/battlefield {:cube-1 {:unit/player 1
                                           :unit/id 1
                                           :object/terrain {:entity/class :terrain}}}
               :game/units {1 {:counter 1 :cubes {1 :cube-1}}}})
 => :unselect



 (provided
  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 {:entity/class :terrain
                                         :entity/state :selectable}}
             :game/units {1 {:counter 1 :cubes {}}}})
  => :unselect))


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
            :game/battlefield battlefield}
           :cube-1)
   => {:game/phase :movement
       :game/subphase :reform
       :game/battlefield battlefield
       :game/selected :cube-1
       :game/battlemap :battlemap-2
       :game/movement {:pointer pointer}}

   (provided
    (mlm/show-reform battlefield :cube-1) => :battlemap-1
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
                 :game/battlefield {:cube-1 :unit-1}})
 => :unselect

 (provided
  (ce/reset-default :unit-1) => :reset-unit-1

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 :reset-unit-1}})
  => :unselect))


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

 (let [pointer (mc/->Pointer :cube-2 :n)
       unit {:unit/player 1
             :unit/id 2
             :entity/presentation :selected}]

   (finish-movement {:game/selected :cube-1
                     :game/battlefield {:cube-1 unit}
                     :game/movement {:pointer pointer}})
   => :unselect

   (provided
    (me/gen-terrain :cube-1) => :terrain

    (ce/reset-default unit) => (assoc unit :entity/presentation :default)

    (unselect {:game/selected :cube-1
               :game/battlefield {:cube-1 :terrain
                                  :cube-2 {:unit/player 1
                                           :unit/id 2
                                           :entity/cube :cube-2
                                           :unit/facing :n
                                           :entity/presentation :default}}
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
    (mlm/show-reform battlefield :cube-1)
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
    (mlm/show-forward battlefield :cube-1)
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
    (mlm/show-reposition battlefield :cube-1)
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
    (mlm/show-march battlefield :cube-1)
    => {:battlemap {:cube-1 :battlemap-entry}
        :breadcrumbs :breadcrumbs-1}

    (mlm/show-threats battlefield :cube-1)
    => {}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap {:cube-1 :battlemap-entry}
           :game/movement {:battlemap {:cube-1 :battlemap-entry}
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
    (mlm/show-march battlefield :cube-1)
    => {:battlemap {:cube-1 :battlemap-entry}
        :breadcrumbs :breadcrumbs-1}

    (mlm/show-threats battlefield :cube-1)
    => {:cube-2 :threat-entry}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap {:cube-1 :battlemap-entry
                            :cube-2 :threat-entry}
           :game/movement {:battlemap {:cube-1 :battlemap-entry
                                       :cube-2 :threat-entry}
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
    (mlm/show-march battlefield :cube-1)
    => {:battlemap {:cube-1 :battlemap-entry}
        :breadcrumbs :breadcrumbs-1}

    (mlm/show-threats battlefield :cube-1)
    => {:cube-2 :threat-entry}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap {:cube-1 :battlemap-entry
                            :cube-2 :threat-entry}
           :game/movement {:battlemap {:cube-1 :battlemap-entry
                                       :cube-2 :threat-entry}
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
    (mlm/show-march battlefield :cube-1)
    => {:battlemap {:cube-1 :battlemap-entry}
        :breadcrumbs :breadcrumbs-1}

    (mlm/show-threats battlefield :cube-1)
    => {:cube-2 :threat-entry}

    (move {:game/phase :movement
           :game/subphase :march
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap {:cube-1 :battlemap-entry
                            :cube-2 :threat-entry}
           :game/movement {:battlemap {:cube-1 :battlemap-entry
                                       :cube-2 :threat-entry}
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
