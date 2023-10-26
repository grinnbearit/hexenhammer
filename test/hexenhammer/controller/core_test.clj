(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.controller.movement :as cm]
            [hexenhammer.controller.core :refer :all]))


(facts
 "to setup"

 (to-setup {})
 => {:game/battlemap :battlemap-2}

 (provided
  (t/reset-battlemap {:game/phase [:setup :select-hex]})
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :silent-selectable)
  => :battlemap-2))


(facts
 "to movement"

 (to-movement {:game/battlefield :battlefield-1
               :game/units :units-1})
 => {:game/battlemap :battlemap-2}

 (provided
  (tu/unit-cubes :units-1 1) => [:cube-1 :cube-2 :cube-3]
  (lbu/movable? :battlefield-1 :cube-1) => true
  (lbu/movable? :battlefield-1 :cube-2) => true
  (lbu/movable? :battlefield-1 :cube-3) => false
  (lbu/unit-key :battlefield-1 :cube-1) => :unit-key-1
  (lbu/unit-key :battlefield-1 :cube-2) => :unit-key-2

  (t/reset-battlemap {:game/battlefield :battlefield-1
                      :game/units :units-1
                      :game/player 1
                      :game/phase [:movement :select-hex]
                      :game/movement {:movable-keys #{:unit-key-1 :unit-key-2}
                                      :movable-cubes #{:cube-1 :cube-2}}}
                     [:cube-1 :cube-2])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selectable) => :battlemap-2))
