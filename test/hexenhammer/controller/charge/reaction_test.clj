(ns hexenhammer.controller.charge.reaction-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.charge.reaction :refer :all]))


(facts
 "unselect"

 (let [targets #{:cube-2}
       state {:game/charge {:targets targets}
              :game/cube :cube-1}]

   (unselect state)
   => {:game/battlemap :battlemap-2}

   (provided
    (tsb/reset-battlemap {:game/charge {:targets targets}
                          :game/phase [:charge :reaction :select-hex]}
                         targets)
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 :selectable)
    => :battlemap-2)))


(facts
 "select hex"

 (select-hex {} :cube-1)
 => {:game/battlemap :battlemap-2}

 (provided
  (tsb/reset-battlemap {:game/cube :cube-1
                        :game/phase [:charge :reaction :react]}
                       [:cube-1])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selected)
  => :battlemap-2))


(facts
 "select react"

 (select-react :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))
