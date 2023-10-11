(ns hexenhammer.controller.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.core :as l]
            [hexenhammer.logic.unit :as lu]
            [hexenhammer.controller.unit :as cu]
            [hexenhammer.controller.event :refer :all]))


(facts
 "panic trigger"

 (let [state {:game/battlefield :battlefield}
       unit {:unit/player 1
             :entity/cube :cube-2}]

   (panic-trigger state unit)
   => :cube-1

   (provided
    (l/enemy-player 1) => 2
    (cu/unit-cubes state 2) => [:cube-1]
    (lu/panic-trigger :battlefield :cube-2 [:cube-1]) => :cube-1))


  (let [state {:game/battlefield :battlefield}
       unit {:unit/player 1
             :entity/cube :cube-2}]

   (panic-trigger state unit)
   => :cube-2

   (provided
    (l/enemy-player 1) => 2
    (cu/unit-cubes state 2) => [])))
