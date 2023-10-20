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
          :game/battlefield {:cube-1 {:entity/class :terrain}}
          :game/battlemap :battlemap-1}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :add-unit
     :game/selected :cube-1
     :game/battlefield {:cube-1 {:entity/class :terrain}}
     :game/battlemap :battlemap-2}

 (provided
  (l/set-state :battlemap-1 [:cube-1] :selected) => :battlemap-2)

 (select {:game/phase :setup
          :game/subphase :select-hex
          :game/battlefield {:cube-1 {:entity/class :unit}}
          :game/battlemap :battlemap-1}
         :cube-1)
 => {:game/phase :setup
     :game/subphase :remove-unit
     :game/selected :cube-1
     :game/battlefield {:cube-1 {:entity/class :unit}}
     :game/battlemap :battlemap-2}

 (provided
  (l/set-state :battlemap-1 [:cube-1] :selected) => :battlemap-2))


(facts
 "unselect setup"

 (unselect {:game/phase :setup
            :game/selected :cube-1
            :game/battlemap :battlemap-1})
 => {:game/phase :setup
     :game/subphase :select-hex
     :game/battlemap :battlemap-2}

 (provided
  (l/set-state :battlemap-1 [:cube-1] :silent-selectable) => :battlemap-2))


(facts
 "select setup add unit"

 (let [state {:game/phase :setup
              :game/subphase :add-unit
              :game/selected :cube-1}]

   (select state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))

 (let [state-1 {:game/phase :setup
                :game/subphase :add-unit
                :game/selected :cube-1}

       state-2 (assoc state-1 :game/selected :cube-2)]

   (select state-1 :cube-2)
   => :unselect

   (provided
    (unselect state-1) => state-2
    (unselect state-2) => :unselect)))


(facts
 "select setup remove unit"

 (let [state {:game/phase :setup
              :game/subphase :remove-unit
              :game/selected :cube-1}]

   (select state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))

 (let [state-1 {:game/phase :setup
                :game/subphase :remove-unit
                :game/selected :cube-1}

       state-2 (assoc state-1 :game/selected :cube-2)]

   (select state-1 :cube-2)
   => :unselect

   (provided
    (unselect state-1) => state-2
    (unselect state-2) => :unselect)))


(facts
 "add unit"

 (add-unit {:game/selected :cube-1
            :game/battlefield {:cube-1 :terrain-1}}
           1
           :facing-1
           {:M 3 :Ld 7 :R 4})
 => :unselect

 (provided
  (me/gen-infantry :cube-1 1 1 :facing-1 :M 3 :Ld 7 :R 4)
  => :unit-1

  (lt/swap :terrain-1 :unit-1)
  => :swap

  (cb/refresh-battlemap {:game/selected :cube-1
                         :game/battlefield {:cube-1 :swap}
                         :game/units {1 {"infantry" {:counter 1 :cubes {1 :cube-1}}}}}
                        [:cube-1])
  => {:game/battlemap :battlemap-1}

  (l/set-state :battlemap-1 [:cube-1] :selectable)
  => :battlemap-2

  (unselect {:game/battlemap :battlemap-2})
  => :unselect))


(facts
 "remove unit"

 (let [unit {:unit/player 1
             :entity/name "unit"
             :unit/id 1}

       battlefield {:cube-1 unit}]

   (remove-unit {:game/selected :cube-1
                 :game/battlefield battlefield
                 :game/units {1 {"unit" {:counter 1 :cubes {1 :cube-1}}}}})
   => :unselect

   (provided

    (lu/remove-unit battlefield :cube-1) => {:cube-1 :terrain}

    (cb/refresh-battlemap {:game/selected :cube-1
                           :game/battlefield {:cube-1 :terrain}
                           :game/units {1 {"unit" {:counter 1 :cubes {}}}}}
                          [:cube-1])
    => {:game/battlemap :battlemap-1}

    (l/set-state :battlemap-1 [:cube-1] :selectable)
    => :battlemap-2

    (unselect {:game/battlemap :battlemap-2})
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

  (cb/refresh-battlemap {:game/selected :cube-1
                         :game/battlefield {:cube-1 :open-terrain}}
                        [:cube-1])
  => :refresh-battlemap

  (unselect :refresh-battlemap)
  => :unselect)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :dangerous)
 => :unselect

 (provided
  (me/gen-dangerous-terrain :cube-1) => :dangerous-terrain

  (le/terrain? :entity-1) => true

  (cb/refresh-battlemap {:game/selected :cube-1
                         :game/battlefield {:cube-1 :dangerous-terrain}}
                        [:cube-1])
  => :refresh-battlemap

  (unselect :refresh-battlemap)
  => :unselect)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :impassable)
 => :unselect

 (provided
  (me/gen-impassable-terrain :cube-1) => :impassable-terrain

  (le/terrain? :entity-1) => true

  (cb/refresh-battlemap {:game/selected :cube-1
                         :game/battlefield {:cube-1 :impassable-terrain}}
                        [:cube-1])
  => :refresh-battlemap

  (unselect :refresh-battlemap)
  => :unselect)


 (swap-terrain {:game/selected :cube-1
                :game/battlefield {:cube-1 :entity-1}}
               :open)
 => :unselect

 (provided
  (me/gen-open-ground :cube-1) => :open-terrain

  (le/terrain? :entity-1) => false

  (lt/place :open-terrain :entity-1) => :entity-2

  (cb/refresh-battlemap {:game/selected :cube-1
                         :game/battlefield {:cube-1 :entity-2}}
                        [:cube-1])
  => :refresh-battlemap

  (unselect :refresh-battlemap)
  => :unselect))


(facts
 "trigger"

 (let [callback (fn [state] (assoc state :game/battlemap :battlemap-2))]

   (trigger {:game/phase :main
             :game/subphase :sub
             :game/events []
             :game/trigger {:callback callback}
             :game/battlemap :battlemap-1}))
 => {:game/events []
     :game/battlemap :battlemap-2}


 (trigger {:game/events [{:event/class :event-1}
                         {:event/class :event-2}]
           :game/trigger {:event {}
                          :callback :callback}
           :game/battlemap :battlemap})
 => :trigger-event

 (provided
  (trigger-event {:game/phase :event-2
                  :game/subphase :start
                  :game/events [{:event/class :event-1}]
                  :game/trigger {:callback :callback}}
                 {:event/class :event-2})
  => :trigger-event))


(facts
 "trigger event dangerous"

 (let [state {:game/phase :dangerous}]

   (trigger-event state {:event/unit-key :unit-key-1})
   => :trigger

   (provided
    (cu/key->cube state :unit-key-1) => nil
    (trigger state) => :trigger))


 (let [state {:game/phase :dangerous
              :game/battlefield {:cube-2 :unit-1}}]

   (trigger-event state {:event/cube :cube-1
                         :event/unit-key :unit-key-1})
   => {:game/battlemap :set-state}

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2
    (mu/models :unit-1) => 3
    (cd/roll! 3) => :roll
    (cd/matches :roll 1) => 3

    (cu/destroy-unit state :cube-2) => {}

    (cb/refresh-battlemap {:game/trigger {:event {:models-destroyed 3
                                                  :unit-destroyed? true
                                                  :roll :roll
                                                  :unit :unit-1}}}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1 :cube-2] :marked) => :set-state))


 (let [state {:game/phase :dangerous
              :game/battlefield {:cube-2 :unit-1}}]

   (trigger-event state {:event/cube :cube-1 :event/unit-key :unit-key-1})
   => {:game/battlemap :set-state}

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2
    (mu/models :unit-1) => 4
    (cd/roll! 4) => :roll
    (cd/matches :roll 1) => 3

    (cu/destroy-models state :cube-2 :cube-1 3) => {}

    (cb/refresh-battlemap {:game/trigger {:event {:models-destroyed 3
                                                  :unit-destroyed? false
                                                  :roll :roll
                                                  :unit :unit-1}}}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1 :cube-2] :marked) => :set-state)))


(facts
 "trigger event opportunity-attack"

 (let [state {:game/phase :opportunity-attack}]

   (trigger-event state {:event/unit-key :unit-key-1})
   => :trigger

   (provided
    (cu/key->cube state :unit-key-1) => nil
    (trigger state) => :trigger))


 (let [state {:game/phase :opportunity-attack
              :game/battlefield {:cube-2 :unit-1}}]

   (trigger-event state {:event/cube :cube-1
                         :event/unit-key :unit-key-1
                         :event/wounds 4})
   => {:game/battlemap :set-state}

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2

    (mu/wounds :unit-1) => 3

    (cu/destroy-unit state :cube-2) => {}

    (cb/refresh-battlemap {:game/trigger {:event {:wounds 4
                                                  :unit-destroyed? true
                                                  :unit :unit-1}}}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1 :cube-2] :marked) => :set-state))


 (let [state {:game/phase :opportunity-attack
              :game/battlefield {:cube-2 :unit-1}}]

   (trigger-event state {:event/cube :cube-1
                         :event/unit-key :unit-key-1
                         :event/wounds 2})
   => {:game/battlemap :set-state}

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2

    (mu/wounds :unit-1) => 3

    (cu/damage-unit state :cube-2 :cube-1 2) => {}

    (cb/refresh-battlemap {:game/trigger {:event {:wounds 2
                                                  :unit-destroyed? false
                                                  :unit :unit-1}}}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1 :cube-2] :marked) => :set-state)))


(facts
 "trigger event heavy casualties"

 (let [state {:game/phase :heavy-casualties}]

   (trigger-event state {:event/unit-key :unit-key-1})
   => :trigger

   (provided
    (cu/key->cube state :unit-key-1) => nil
    (trigger state) => :trigger))


 (let [state {:game/phase :heavy-casualties
              :game/battlefield :battlefield}]

   (trigger-event state {:event/cube :cube-1
                         :event/unit-key :unit-key-1})
   => :trigger

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2
    (lu/panickable? :battlefield :cube-2) => false
    (trigger state) => :trigger))


 (let [battlefield {:cube-2 {:unit/Ld 3}}

       state {:game/phase :heavy-casualties
              :game/battlefield battlefield}]

   (trigger-event state {:event/cube :cube-1
                         :event/unit-key :unit-key-1})
   => {:game/battlemap :set-state}

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2
    (lu/panickable? battlefield :cube-2) => true
    (cd/roll! 2) => [1 1]

    (cb/refresh-battlemap {:game/phase :heavy-casualties
                           :game/battlefield {:cube-2 {:unit/Ld 3
                                                       :unit/phase {:panicked? true}}}
                           :game/subphase :passed
                           :game/trigger {:event {:trigger-cube :cube-1
                                                  :unit-cube :cube-2
                                                  :roll [1 1]}}}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1 :cube-2] :marked) => :set-state))


 (let [battlefield {:cube-2 {:unit/Ld 3}}

       state {:game/phase :heavy-casualties
              :game/battlefield battlefield}]

   (trigger-event state {:event/cube :cube-1
                         :event/unit-key :unit-key-1})
   => {:game/battlemap :set-state}

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2
    (lu/panickable? battlefield :cube-2) => true
    (cd/roll! 2) => [1 3]

    (cb/refresh-battlemap {:game/phase :heavy-casualties
                           :game/battlefield {:cube-2 {:unit/Ld 3
                                                       :unit/phase {:panicked? true}}}
                           :game/subphase :failed
                           :game/trigger {:event {:trigger-cube :cube-1
                                                  :unit-cube :cube-2
                                                  :roll [1 3]}}}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1 :cube-2] :marked) => :set-state)))


(facts
 "trigger event panic"

 (let [state {:game/phase :panic}]

   (trigger-event state {:event/unit-key :unit-key-1})
   => :trigger

   (provided
    (cu/key->cube state :unit-key-1) => nil
    (trigger state) => :trigger))


 (let [state {:game/phase :panic
              :game/battlefield :battlefield}]

   (trigger-event state {:event/cube :cube-1 :event/unit-key :unit-key-1})
   => :trigger

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2
    (lu/panickable? :battlefield :cube-2) => false
    (trigger state) => :trigger))


 (let [unit {:unit/Ld 3}

       battlefield {:cube-2 unit}

       state {:game/phase :panic
              :game/battlefield battlefield}]

   (trigger-event state {:event/cube :cube-1 :event/unit-key :unit-key-1})
   => {:game/battlemap :set-state}

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2
    (lu/panickable? battlefield :cube-2) => true
    (cd/roll! 2) => [1 1]

    (cb/refresh-battlemap {:game/phase :panic
                           :game/battlefield {:cube-2 {:unit/Ld 3
                                                       :unit/phase {:panicked? true}}}
                           :game/subphase :passed
                           :game/trigger {:event {:unit-cube :cube-2
                                                  :roll [1 1]}}}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1 :cube-2] :marked) => :set-state))


 (let [unit {:unit/Ld 3}

       battlefield {:cube-2 unit}

       state {:game/phase :panic
              :game/battlefield battlefield}]

   (trigger-event state {:event/cube :cube-1 :event/unit-key :unit-key-1})
   => {:game/battlemap :set-state}

   (provided
    (cu/key->cube state :unit-key-1) => :cube-2
    (lu/panickable? battlefield :cube-2) => true
    (cd/roll! 2) => [1 3]

    (cb/refresh-battlemap {:game/phase :panic
                           :game/battlefield {:cube-2 {:unit/Ld 3
                                                       :unit/phase {:panicked? true}}}
                           :game/subphase :failed
                           :game/trigger {:event {:unit-cube :cube-2
                                                  :roll [1 3]}}}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap}

    (l/set-state :battlemap [:cube-1 :cube-2] :marked) => :set-state)))


(facts
 "reset charge"

 (let [state {:game/player 1
              :game/battlefield :battlefield-1}]

   (reset-charge state)
   => {:game/battlemap :battlemap-2}

   (provided
    (cu/unit-cubes state 1) => [:cube-1 :cube-2]
    (lm/charger? :battlefield-1 :cube-1) => true
    (lm/charger? :battlefield-1 :cube-2) => false

    (cb/refresh-battlemap {:game/player 1
                           :game/battlefield :battlefield-1
                           :game/phase :charge
                           :game/subphase :select-hex
                           :game/charge {:chargers #{:cube-1}}}
                          [:cube-1])
    => {:game/battlemap :battlemap-1}

    (l/set-state :battlemap-1 [:cube-1] :selectable)
    => :battlemap-2)))


(facts
 "to charge"

 (let [state {:game/battlefield :battlefield-1}]

   (to-charge state)
   => :reset-charge

   (provided
    (cu/unit-cubes state) => [:cube-1]
    (lu/phase-reset :battlefield-1 [:cube-1]) => :battlefield-2
    (reset-charge {:game/battlefield :battlefield-2}) => :reset-charge)))


(facts
 "unselect charge"

 (unselect {:game/phase :charge
            :game/charge {:chargers [:cube-1]
                          :pointer :pointer}
            :game/selected :cube-1
            :game/battlemap :battlemap-1})

 => {:game/battlemap :battlemap-3}

 (provided
  (cb/refresh-battlemap {:game/phase :charge
                         :game/charge {:chargers [:cube-1]}
                         :game/subphase :select-hex}
                        [:cube-1])
  => {:game/battlemap :battlemap-2}

  (l/set-state :battlemap-2 [:cube-1] :selectable)
  => :battlemap-3))


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
                   :pointer->targets :pointer->targets-1
                   :pointer->range :pointer->range-1}}

 (provided
  (lm/show-charge :battlefield-1 :cube-1) => {:battlemap :battlemap-1
                                              :breadcrumbs :breadcrumbs-1
                                              :pointer->events :pointer->events-1
                                              :pointer->targets :pointer->targets-1
                                              :pointer->range :pointer->range-1}))


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
 "unselect react"

 (let [state {:game/phase :react
              :game/react {:targets [:cube-1 :cube-2]}
              :game/selected :cube-3
              :game/battlemap :battlemap-1}]

   (unselect state)
   => {:game/battlemap :battlemap-3}

   (provided
    (cb/refresh-battlemap {:game/phase :react
                           :game/react {:targets [:cube-1 :cube-2]}
                           :game/subphase :select-hex}
                          [:cube-1 :cube-2])
    => {:game/battlemap :battlemap-2}

    (l/set-state :battlemap-2 [:cube-1 :cube-2] :selectable)
    => :battlemap-3)))


(facts
 "reset react"

 (let [state {:game/battlefield :battlefield-1
              :game/react {:charger :cube-1
                           :declared #{:unit-key-1 :unit-key-2 :unit-key-3 :unit-key-4}
                           :reacted #{:unit-key-1}
                           :flee :flee-1}}]

   (reset-react state)
   => :unselect

   (provided
    (cu/key->cube state :unit-key-2) => nil
    (cu/key->cube state :unit-key-3) => :cube-3
    (cu/key->cube state :unit-key-4) => :cube-4

    (lm/reactive? :battlefield-1 :cube-3) => false
    (lm/reactive? :battlefield-1 :cube-4) => true

    (unselect {:game/phase :react
               :game/battlefield :battlefield-1
               :game/react {:charger :cube-1
                            :declared #{:unit-key-1 :unit-key-2 :unit-key-3 :unit-key-4}
                            :reacted #{:unit-key-1}
                            :targets #{:cube-4}}})
    => :unselect)))


(facts
 "declare charge"

 (let [pointer (mc/->Pointer :cube-1 :n)
       state {:game/selected :cube-1
              :game/battlefield {:cube-2 :unit-1
                                 :cube-3 :unit-2}
              :game/charge {:pointer pointer
                            :pointer->targets {pointer #{:cube-2 :cube-3}}}}]


   (declare-charge state)
   => :reset-react


   (provided
    (mu/unit-key :unit-1) => :unit-key-1
    (mu/unit-key :unit-2) => :unit-key-2

    (reset-react {:game/phase :react
                  :game/selected :cube-1
                  :game/battlefield {:cube-2 :unit-1
                                     :cube-3 :unit-2}
                  :game/react {:charger :cube-1
                               :declared #{:unit-key-1 :unit-key-2}
                               :reacted #{}}})
    => :reset-react)))


(facts
 "react transition"

 (react-transition {:game/selected :cube-1
                    :game/subphase :select-hex}
                   :flee)
 => :select

 (provided
  (select {:game/subphase :flee}
          :cube-1)
  => :select))


(facts
 "select react select-hex"

 (select {:game/selected :cube-1
          :game/phase :react
          :game/subphase :select-hex}
         :cube-1)
 => :unselect

 (provided
  (unselect {:game/selected :cube-1
             :game/phase :react
             :game/subphase :hold})
  => :unselect))


(facts
 "select react hold"

 (let [state {:game/phase :react
              :game/subphase :hold
              :game/selected :cube-1}]

   (select state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))


 (let [state {:game/phase :react
              :game/subphase :hold
              :game/selected :cube-1
              :game/battlemap :battlemap-1}]

   (select state :cube-2)
   => {:game/battlemap :battlemap-3}

   (provided
    (cb/refresh-battlemap {:game/phase :react
                           :game/subphase :hold
                           :game/selected :cube-2}
                          [:cube-2])
    => {:game/battlemap :battlemap-2}

    (l/set-state :battlemap-2 [:cube-2] :selected)
    => :battlemap-3)))


(facts
 "select react flee"

 (let [state {:game/phase :react
              :game/subphase :flee
              :game/selected :cube-1}]

   (select state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect))


 (let [state {:game/phase :react
              :game/subphase :flee
              :game/selected :cube-1
              :game/battlefield :battlefield-1
              :game/react {:charger :cube-3}}]

   (select state :cube-2)
   => {:game/phase :react
       :game/subphase :flee
       :game/selected :cube-2
       :game/battlefield :battlefield-1
       :game/battlemap :battlemap-2
       :game/react {:charger :cube-3}}

   (provided
    (lm/show-flee-direction :battlefield-1 :cube-2 :cube-3)
    => {:battlemap :battlemap-1}

    (l/set-state :battlemap-1 [:cube-2] :selected)
    => :battlemap-2)))


(facts
 "react hold"

 (let [state {:game/selected :cube-1
              :game/battlefield {:cube-1 :unit-1}
              :game/react {:targets #{:cube-1 :cube-2}
                           :reacted #{}}}]

   (react-hold state)
   => :unselect

   (provided
    (mu/unit-key :unit-1) => :unit-key-1

    (unselect {:game/selected :cube-1
               :game/battlefield {:cube-1 :unit-1}
               :game/react {:targets #{:cube-2}
                            :reacted #{:unit-key-1}}})
    => :unselect)))


(facts
 "react flee"

 (let [battlefield {:cube-2 :unit-1}
       state {:game/selected :cube-2
              :game/react {:charger :cube-1}
              :game/battlefield battlefield}
       end (mc/->Pointer :cube-3 :n)]

   (react-flee state)
   => {:game/battlemap :battlemap-2}

   (provided
    (mu/unit-key :unit-1) => :unit-key-1

    (cd/roll! 2) => [2 3]

    (lm/show-flee battlefield :cube-2 :cube-1 5)
    => {:battlemap :battlemap-1
        :end end
        :edge? true
        :events [:event-1]}

    (cu/escape-unit state :cube-2 :cube-3)
    => {:game/events []
        :game/react {:reacted #{}}}

    (cb/refresh-battlemap {:game/react {:flee {:edge? true
                                               :unit :unit-1
                                               :roll [2 3]}
                                        :reacted #{:unit-key-1}}
                           :game/events [:event-1]
                           :game/battlemap :battlemap-1
                           :game/subphase :fled}
                          [:cube-1 :cube-3])
    => {:game/battlemap :battlemap-1}

    (l/set-state :battlemap-1 [:cube-1 :cube-3] :marked)
    => :battlemap-2))


 (let [unit {:entity/class :unit}
       battlefield {:cube-2 unit}
       state {:game/selected :cube-2
              :game/react {:charger :cube-1}
              :game/battlefield battlefield}
       end (mc/->Pointer :cube-3 :n)]

   (react-flee state)
   => {:game/battlemap :battlemap-3}

   (provided
    (mu/unit-key {:entity/class :unit}) => :unit-key-1

    (cd/roll! 2) => [2 3]

    (lm/show-flee battlefield :cube-2 :cube-1 5)
    => {:battlemap :battlemap-1
        :end end
        :edge? false
        :events [:event-1]}

    (cu/move-unit {:game/selected :cube-2
                   :game/react {:charger :cube-1}
                   :game/battlefield {:cube-2 {:entity/class :unit
                                               :unit/movement {:fleeing? true}}}}
                  :cube-2 end)
    => {:game/events []
        :game/react {:reacted #{}}}

    (cb/refresh-battlemap {:game/events [:event-1]
                           :game/battlemap :battlemap-1
                           :game/subphase :fled
                           :game/react {:flee {:edge? false
                                               :unit unit
                                               :roll [2 3]}
                                        :reacted #{:unit-key-1}}}
                          [:cube-1 :cube-3])
    => {:game/battlemap :battlemap-2}

    (l/set-state :battlemap-2 [:cube-1 :cube-3] :marked)
    => :battlemap-3)))


(facts
 "react trigger"

 (react-trigger :state)
 => :trigger

 (provided
  (trigger :state reset-react) => :trigger))


(facts
 "reset movement"

 (let [state {:game/player 1
              :game/battlefield :battlefield-1}]

   (reset-movement state)
   => {:game/battlemap :battlemap-2}

   (provided
    (cu/unit-cubes state 1) => [:unit-cube-1 :unit-cube-2]

    (lm/movable? :battlefield-1 :unit-cube-1)
    => true

    (lm/movable? :battlefield-1 :unit-cube-2)
    => false

    (cb/refresh-battlemap {:game/player 1
                           :game/battlefield :battlefield-1
                           :game/phase :movement
                           :game/subphase :select-hex
                           :game/movement {:movers #{:unit-cube-1}}}
                          [:unit-cube-1])
    => {:game/battlemap :battlemap-1}

    (l/set-state :battlemap-1 [:unit-cube-1] :selectable)
    => :battlemap-2)))


(facts
 "to movement"

 (to-movement {:game/charge :charge})
 => :reset-movement

 (provided
  (reset-movement {}) => :reset-movement))


(facts
 "select movement select-hex"

 (let [battlefield {:cube-1 {:unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)]

   (select {:game/phase :movement
            :game/subphase :select-hex
            :game/battlefield battlefield}
           :cube-1)
   => :move

   (provided
    (lm/show-reform battlefield :cube-1) => {:battlemap :battlemap-1 :pointer->events :events-1}

    (move {:game/phase :movement
           :game/subphase :reform
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :pointer->events :events-1}}
          pointer)
    => :move)))


(facts
 "unselect movement"

 (unselect {:game/phase :movement
            :game/movement {:movers [:cube-1]
                            :pointer :pointer}
            :game/selected :cube-1
            :game/battlemap :battlemap-1})

 => {:game/battlemap :battlemap-3}

 (provided
  (cb/refresh-battlemap {:game/phase :movement
                         :game/movement {:movers [:cube-1]}
                         :game/subphase :select-hex}
                        [:cube-1])
  => {:game/battlemap :battlemap-2}

  (l/set-state :battlemap-2 [:cube-1] :selectable)
  => :battlemap-3))


(facts
 "skip movement"

 (skip-movement {:game/selected :cube-1
                 :game/battlefield {:cube-1 {}}
                 :game/movement {:movers #{:cube-1 :cube-2}}})
 => :unselect

 (provided

  (unselect {:game/selected :cube-1
             :game/battlefield {:cube-1 {:unit/movement {:unmoved? true}}}
             :game/movement {:movers #{:cube-2}}})
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
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:unit/facing :n}}
          :game/movement {:battlemap :battlemap-1
                          :moved? true}}
         pointer)
   => {:game/phase :movement
       :game/subphase :reform
       :game/selected :cube-1
       :game/battlefield {:cube-1 {:unit/facing :n}}
       :game/battlemap :battlemap-2
       :game/movement {:battlemap :battlemap-1
                       :pointer pointer}}

   (provided
    (cm/set-mover-selected :battlemap-1 pointer) => :battlemap-2))


 (let [pointer (mc/->Pointer :cube-1 :s)]

   (move {:game/phase :movement
          :game/subphase :reform
          :game/selected :cube-1
          :game/battlefield {:cube-1 {:unit/facing :n}}
          :game/movement {:battlemap :battlemap-1
                          :pointer pointer}}
         pointer)
   => {:game/phase :movement
       :game/subphase :reform
       :game/selected :cube-1
       :game/battlefield {:cube-1 {:unit/facing :n}}
       :game/battlemap :battlemap-2
       :game/movement {:battlemap :battlemap-1
                       :moved? true
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

 (let [pointer (mc/->Pointer :cube-1 :n)]

   (finish-movement {:game/selected :cube-1
                     :game/battlefield {:cube-1 {:entity/class :unit}}
                     :game/movement {:pointer pointer
                                     :pointer->events {pointer [:event-1 :event-2]}}
                     :game/events []})
   => :trigger

   (provided
    (cu/move-unit {:game/selected :cube-1
                   :game/battlefield {:cube-1 {:entity/class :unit
                                               :unit/movement {:marched? false
                                                               :moved? true}}}
                   :game/movement {:pointer pointer
                                   :pointer->events {pointer [:event-1 :event-2]}}
                   :game/events []}
                  :cube-1
                  pointer)
    => {:game/events []}

    (unselect {:game/events [:event-1 :event-2]})
    => :unselect

    (trigger :unselect reset-movement)
    => :trigger))


 (let [pointer (mc/->Pointer :cube-1 :n)]

   (finish-movement {:game/selected :cube-1
                     :game/battlefield {:cube-1 {:entity/class :unit}}
                     :game/movement {:pointer pointer
                                     :pointer->events {pointer []}
                                     :marched? true}
                     :game/events []})
   => :trigger

   (provided
    (cu/move-unit {:game/selected :cube-1
                   :game/battlefield {:cube-1 {:entity/class :unit
                                               :unit/movement {:marched? true
                                                               :moved? true}}}
                   :game/movement {:pointer pointer
                                   :pointer->events {pointer []}
                                   :marched? true}
                   :game/events []}
                  :cube-1
                  pointer)
    => {:game/events []}

    (unselect {:game/events []})
    => :unselect

    (trigger :unselect reset-movement)
    => :trigger)))


(facts
 "movement transition"

 (movement-transition {:game/movement {:pointer :pointer-1
                                       :movers [:cube-1]}
                       :game/selected :cube-1}
                      :movement-1)
 => [:select :movement-1]

 (provided
  (select {:game/subphase :movement-1
           :game/selected :cube-1
           :game/movement {:movers [:cube-1]}}
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
    => {:battlemap :battlemap-1 :pointer->events :events-1}

    (move {:game/phase :movement
           :game/subphase :reform
           :game/battlefield battlefield
           :game/selected :cube-1
           :game/battlemap :battlemap-1
           :game/movement {:battlemap :battlemap-1
                           :pointer->events :events-1}}
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


(facts
 "flee heavy-casualties"

 (let [end (mc/->Pointer :cube-3 :n)
       battlefield {:cube-2 :unit}
       state {:game/phase :heavy-casualties
              :game/trigger {:event {:trigger-cube :cube-1
                                     :unit-cube :cube-2}}
              :game/battlefield battlefield}]

   (flee state)
   => {:game/battlemap :battlemap-2
       :game/trigger {:event {:edge? true
                              :unit :unit
                              :roll [2 3]}}}

   (provided
    (cd/roll! 2) => [2 3]

    (lm/show-flee battlefield :cube-2 :cube-1 5)
    => {:battlemap :battlemap-1
        :end end
        :edge? true
        :events [:event-1]}

    (cu/destroy-unit state :cube-2)
    => {:game/events []}

    (cb/refresh-battlemap {:game/events [:event-1]
                           :game/battlemap :battlemap-1
                           :game/subphase :flee}
                          [:cube-3])
    => {:game/battlemap :battlemap-1}

    (l/set-state :battlemap-1 [:cube-3] :marked)
    => :battlemap-2))


 (let [end (mc/->Pointer :cube-3 :n)
       unit {:entity/class :unit}
       battlefield {:cube-2 unit}
       state {:game/phase :heavy-casualties
              :game/trigger {:event {:trigger-cube :cube-1
                                     :unit-cube :cube-2}}
              :game/battlefield battlefield}]

   (flee state)
   => {:game/battlemap :battlemap-3
       :game/trigger {:event {:edge? false
                              :unit unit
                              :roll [2 3]}}}

   (provided
    (cd/roll! 2) => [2 3]

    (lm/show-flee battlefield :cube-2 :cube-1 5)
    => {:battlemap :battlemap-1
        :end end
        :edge? false
        :events [:event-1]}

    (cu/move-unit {:game/phase :heavy-casualties
                   :game/trigger {:event {:trigger-cube :cube-1
                                          :unit-cube :cube-2}}
                   :game/battlefield {:cube-2 {:entity/class :unit
                                               :unit/movement {:fleeing? true}}}}
                  :cube-2 end)
    => {:game/events []}

    (cb/refresh-battlemap {:game/events [:event-1]
                           :game/battlemap :battlemap-1
                           :game/subphase :flee}
                          [:cube-3])
    => {:game/battlemap :battlemap-2}

    (l/set-state :battlemap-2 [:cube-3] :marked)
    => :battlemap-3)))


(facts
 "flee panic"

 (let [end (mc/->Pointer :cube-3 :n)
       battlefield {:cube-2 :unit}
       state {:game/phase :panic
              :game/trigger {:event {:unit-cube :cube-2}}
              :game/battlefield battlefield}]

   (flee state)
   => {:game/battlemap :battlemap-2
       :game/trigger {:event {:edge? true
                              :unit :unit
                              :roll [2 3]}}}

   (provided
    (ce/panic-trigger state :unit)
    => :cube-1

    (cd/roll! 2) => [2 3]

    (lm/show-flee battlefield :cube-2 :cube-1 5)
    => {:battlemap :battlemap-1
        :end end
        :edge? true
        :events [:event-1]}

    (cu/escape-unit state :cube-2 :cube-3)
    => {:game/events []}

    (cb/refresh-battlemap {:game/events [:event-1]
                           :game/battlemap :battlemap-1
                           :game/subphase :flee}
                          [:cube-1 :cube-3])
    => {:game/battlemap :battlemap-1}

    (l/set-state :battlemap-1 [:cube-1 :cube-3] :marked)
    => :battlemap-2))


 (let [end (mc/->Pointer :cube-3 :n)
       unit {:entity/class :unit}
       battlefield {:cube-2 unit}
       state {:game/phase :panic
              :game/trigger {:event {:unit-cube :cube-2}}
              :game/battlefield battlefield}]

   (flee state)
   => {:game/battlemap :battlemap-3
       :game/trigger {:event {:edge? false
                              :unit unit
                              :roll [2 3]}}}

   (provided
    (ce/panic-trigger state unit)
    => :cube-1

    (cd/roll! 2) => [2 3]

    (lm/show-flee battlefield :cube-2 :cube-1 5)
    => {:battlemap :battlemap-1
        :end end
        :edge? false
        :events [:event-1]}

    (cu/move-unit {:game/phase :panic
                   :game/trigger {:event {:unit-cube :cube-2}}
                   :game/battlefield {:cube-2 {:entity/class :unit
                                               :unit/movement {:fleeing? true}}}}
                  :cube-2 end)
    => {:game/events []}

    (cb/refresh-battlemap {:game/events [:event-1]
                           :game/battlemap :battlemap-1
                           :game/subphase :flee}
                          [:cube-1 :cube-3])
    => {:game/battlemap :battlemap-2}

    (l/set-state :battlemap-2 [:cube-1 :cube-3] :marked)
    => :battlemap-3)))
