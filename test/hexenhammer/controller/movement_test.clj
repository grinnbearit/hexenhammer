(ns hexenhammer.controller.core-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.controller.movement :refer :all]))


(facts
 "reset movement"

 (let [state #:game{:player 1
                    :battlefield :battlefield-1
                    :units :units-1}]

   (reset-movement state)
   => #:game{:battlemap :battlemap-2}

   (provided
    (tu/unit-cubes :units-1 1) => [:cube-1 :cube-2]

    (lbu/movable? :battlefield-1 :cube-1) => true
    (lbu/movable? :battlefield-1 :cube-2) => false

    (t/reset-battlemap #:game{:player 1
                              :battlefield :battlefield-1
                              :units :units-1
                              :phase [:movement :select-hex]
                              :movement {:movers [:cube-1]}}
                       [:cube-1])
    => #:game{:battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 :marked)
    => :battlemap-2)))
