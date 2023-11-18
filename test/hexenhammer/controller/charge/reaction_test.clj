(ns hexenhammer.controller.charge.reaction-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.charge.reaction :refer :all]))


(facts
 "unselect"

 (let [state {:game/charge {:chargers #{}}
              :game/cube :cube-1
              :game/battlemap :battlemap-1}]

   (unselect state)
   => {:game/phase [:charge :reaction :finish-reaction]})

 (let [targets #{:cube-2 :cube-3}
       reacted #{:cube-2}
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
 "set hold"

 (set-hold {} :cube-1)
 => {:game/battlemap :battlemap-2}

 (provided
  (tsb/reset-battlemap {:game/cube :cube-1
                        :game/phase [:charge :reaction :hold]}
                       [:cube-1])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selected)
  => :battlemap-2))


(facts
 "set flee"

 (set-flee {} :cube-1)
 => {:game/battlemap :battlemap-2}

 (provided
  (tsb/reset-battlemap {:game/cube :cube-1
                        :game/phase [:charge :reaction :flee]}
                       [:cube-1])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selected)
  => :battlemap-2))


(facts
 "select hex"

 (select-hex :state-1 :cube-1)
 => :set-hold

 (provided
  (set-hold :state-1 :cube-1) => :set-hold))


(facts
 "select hold"

 (select-hold :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "select flee"

 (select-flee :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "hold"

 (let [state {:game/cube :cube-1
              :game/charge {:targets #{:cube-1 :cube-2}}}]

   (hold state)
   => :unselect

   (provided
    (unselect {:game/cube :cube-1
               :game/charge {:targets #{:cube-2}}})
    => :unselect)))


(facts
 "switch reaction"

 (let [state {:game/cube :cube-1}]

   (switch-reaction state :hold)
   => :set-hold

   (provided
    (unselect state) => :unselect
    (set-hold :unselect :cube-1) => :set-hold))


 (let [state {:game/cube :cube-1}]

   (switch-reaction state :flee)
   => :set-flee

   (provided
    (unselect state) => :unselect
    (set-flee :unselect :cube-1) => :set-flee)))
