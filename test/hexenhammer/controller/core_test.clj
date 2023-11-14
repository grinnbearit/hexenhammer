(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.core :as lbm]
            [hexenhammer.logic.battlefield.movement.charge :as lbmc]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.battlefield :as tf]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.charge :as cc]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.core :refer :all]))


(facts
 "to setup"

 (to-setup {})
 => {:game/battlemap :battlemap-2}

 (provided
  (tsb/reset-battlemap {:game/phase [:setup :select-hex]})
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :silent-selectable)
  => :battlemap-2))


(facts
 "to start"

 (to-start {})
 => :to-charge

 (provided
  (to-charge {:game/player 1}) => :to-charge))


(facts
 "to charge"

 (to-charge {:game/battlefield :battlefield-1
             :game/units :units-1})
 => :unselect

 (provided
  (tu/unit-cubes :units-1) => [:cube-1 :cube-2 :cube-3 :cube-4]
  (tu/unit-cubes :units-1 1) => [:cube-1 :cube-2 :cube-3]
  (lbmc/charger? :battlefield-1 :cube-1) => true
  (lbmc/charger? :battlefield-1 :cube-2) => true
  (lbmc/charger? :battlefield-1 :cube-3) => false
  (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1
  (lbu/unit-key :battlefield-1 :cube-2) => :unit-key-2

  (tf/reset-phase :battlefield-1 [:cube-1 :cube-2 :cube-3 :cube-4])
  => :battlefield-2

  (cc/unselect {:game/battlefield :battlefield-2
                :game/units :units-1
                :game/phase [:charge :select-hex]
                :game/charge {:charger-keys #{:unit-key-1 :unit-key-2}
                              :charger-cubes #{:cube-1 :cube-2}}})
  => :unselect))


(facts
 "to movement"

 (to-movement {:game/battlefield :battlefield-1
               :game/units :units-1})
 => :unselect

 (provided
  (tu/unit-cubes :units-1 1) => [:cube-1 :cube-2 :cube-3]
  (lbm/movable? :battlefield-1 :cube-1) => true
  (lbm/movable? :battlefield-1 :cube-2) => true
  (lbm/movable? :battlefield-1 :cube-3) => false
  (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1
  (lbu/unit-key :battlefield-1 :cube-2) => :unit-key-2

  (cm/unselect {:game/battlefield :battlefield-1
                :game/units :units-1
                :game/phase [:movement :select-hex]
                :game/movement {:movable-keys #{:unit-key-1 :unit-key-2}
                                :movable-cubes #{:cube-1 :cube-2}}})
  => :unselect))


(facts
 "to close combat"

 (to-close-combat {:game/player 1
                   :game/units :units-1
                   :game/battlefield :battlefield-1
                   :game/movement :movement-1})
 => {:game/player 1
     :game/units :units-1
     :game/battlefield :battlefield-3
     :game/phase [:close-combat]}

 (provided
  (tu/unit-cubes :units-1) => :unit-cubes
  (tu/unit-cubes :units-1 1) => :player-cubes

  (tf/reset-phase :battlefield-1 :unit-cubes) => :battlefield-2
  (tf/reset-movement :battlefield-2 :player-cubes) => :battlefield-3))
