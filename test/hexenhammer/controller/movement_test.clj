(ns hexenhammer.controller.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.core :as t]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.controller.movement :refer :all]))


(facts
 "unselect"

 (unselect {:game/movement {:movable-cubes [:cube-1 :cube-2]}
            :game/cube :cube-1})
 => {:game/battlemap :battlemap-2}

 (provided
  (t/reset-battlemap {:game/movement {:movable-cubes [:cube-1 :cube-2]}
                      :game/phase [:movement :select-hex]}
                     [:cube-1 :cube-2])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selectable)
  => :battlemap-2))


(facts
 "select reform"

 (let [state {:game/cube :cube-1}]

   (select-reform state :cube-1)
   => :unselect

   (provided
    (unselect state) => :unselect)))


(facts
 "select hex"

 (select-hex {} :cube-1)
 => :select-reform

 (provided
  (select-reform {:game/phase [:movement :reform]} :cube-1)
  => :select-reform))
