(ns hexenhammer.controller.charge.reaction-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.flee :as lbmf]
            [hexenhammer.transition.dice :as td]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.units :as tsu]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.event :as ce]
            [hexenhammer.controller.charge.reaction :refer :all]))


(facts
 "unselect"

 (let [state {:game/charge {:chargers #{}}
              :game/cube :cube-1
              :game/battlemap :battlemap-1}]

   (unselect state)
   => {:game/phase [:charge :reaction :finish-reaction]})


 (let [target-cubes #{:cube-2 :cube-3}
       reacted #{:cube-2}
       state {:game/charge {:charger :cube-1
                            :target-cubes target-cubes}
              :game/cube :cube-1}]

   (unselect state)
   => {:game/battlemap :battlemap-3}

   (provided
    (tsb/reset-battlemap {:game/charge {:charger :cube-1
                                        :target-cubes target-cubes}
                          :game/phase [:charge :reaction :select-hex]}
                         #{:cube-1 :cube-2 :cube-3})
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 target-cubes :selectable)
    => :battlemap-2

    (tb/set-presentation :battlemap-2 [:cube-1] :marked)
    => :battlemap-3)))


(facts
 "set hold"

 (let [state {:game/charge {:charger :cube-2}}]

   (set-hold state :cube-1)
   => {:game/battlemap :battlemap-3}

   (provided
    (tsb/reset-battlemap {:game/charge {:charger :cube-2}
                          :game/cube :cube-1
                          :game/phase [:charge :reaction :hold]}
                         [:cube-1 :cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-1] :selected)
    => :battlemap-2

    (tb/set-presentation :battlemap-2 [:cube-2] :marked)
    => :battlemap-3)))


(facts
 "set flee"

 (let [state {:game/battlefield :battlefield-1
              :game/charge {:charger :cube-2}}]

   (set-flee state :cube-1)
   => {:game/battlemap :battlemap-3}

   (provided
    (lbmf/flee :battlefield-1 :cube-1 :cube-2 12)
    => {:cube->tweeners :cube->tweeners-1
        :events :events-1}

    (tsb/refresh-battlemap {:game/battlefield :battlefield-1
                            :game/charge {:charger :cube-2
                                          :events :events-1}
                            :game/cube :cube-1
                            :game/phase [:charge :reaction :flee]
                            :game/battlemap :cube->tweeners-1}
                           [:cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-1] :selected)
    => :battlemap-2

    (tb/set-presentation :battlemap-2 [:cube-2] :marked)
    => :battlemap-3)))


(facts
 "set fled"

 (set-fled :state-1 :cube-1)
 => {:game/phase [:charge :reaction :fled]}

 (provided
  (set-hold :state-1 :cube-1) => {}))


(facts
 "set fleeing"

 (set-fleeing :state-1 :cube-1)
 => {:game/phase [:charge :reaction :fleeing]}

 (provided
  (set-flee :state-1 :cube-1) => {}))


(facts
 "select hex"

 (let [battlefield {:cube-1 :unit-1}
       state {:game/battlefield battlefield}]

   (select-hex state :cube-1)
   => :set-hold

   (provided
    (leu/fleeing? :unit-1) => false
    (set-hold state :cube-1) => :set-hold)


   (select-hex state :cube-1)
   => :set-fleeing

   (provided
    (leu/fleeing? :unit-1) => true
    (leu/fled? :unit-1) => false
    (set-fleeing state :cube-1) => :set-fleeing)


   (select-hex state :cube-1)
   => :set-fled

   (provided
    (leu/fleeing? :unit-1) => true
    (leu/fled? :unit-1) => true
    (set-fled state :cube-1) => :set-fled)))


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
              :game/battlefield :battlefield-1
              :game/charge {:target-keys #{:unit-key-1 :unit-key-2}
                            :target-cubes #{:cube-1 :cube-2}}}]

   (hold state)
   => :unselect

   (provided
    (lbu/unit-key :battlefield-1 :cube-1)
    => :unit-key-1

    (unselect {:game/cube :cube-1
               :game/battlefield :battlefield-1
               :game/charge {:target-keys #{:unit-key-2}
                             :target-cubes #{:cube-2}}})
    => :unselect)))


(facts
 "fled"

 (fled :state-1)
 => :hold

 (provided
  (hold :state-1) => :hold))


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


(facts
 "flee"

 (let [battlefield {:cube-1 :unit-1}
       state {:game/cube :cube-1
              :game/battlefield battlefield
              :game/charge {:charger :cube-2}}
       pointer (lc/->Pointer :cube-3 :n)
       cube->tweeners {:cube-1 :mover-1}]

   (flee state)
   => {:game/battlemap :battlemap-3}

   (provided
    (leu/unit-key :unit-1) => :unit-key-1

    (td/roll! 2) => [1 2]

    (lbmf/flee battlefield :cube-1 :cube-2 3)
    => {:end pointer
        :cube->tweeners cube->tweeners
        :edge? true
        :events [:event-2]}

    (tsu/escape-unit state :cube-1 :cube-3)
    => {:game/events [:event-1]
        :game/charge {:target-keys #{:unit-key-1}
                      :target-cubes #{:cube-1}}}

    (tsb/reset-battlemap {:game/phase [:charge :reaction :flee :roll]
                          :game/events [:event-1 :event-2]
                          :game/charge {:edge? true
                                        :unit :unit-1
                                        :roll [1 2]
                                        :target-keys #{}
                                        :target-cubes #{}}}
                         [:cube-2])
    => {:game/battlemap {:cube-2 :mover-2}}

    (tb/set-presentation {:cube-1 :mover-1
                          :cube-2 :mover-2}
                         [:cube-2 :cube-3]
                         :marked)
    => :battlemap-3))


 (let [battlefield {:cube-1 :unit-1}
       state {:game/cube :cube-1
              :game/charge {:charger :cube-2}
              :game/battlefield battlefield}
       pointer (lc/->Pointer :cube-3 :n)
       cube->tweeners {:cube-1 :mover-1}]

   (flee state)
   => {:game/battlemap :battlemap-3}

   (provided
    (leu/unit-key :unit-1) => :unit-key-1

    (td/roll! 2) => [1 2]

    (lbmf/flee battlefield :cube-1 :cube-2 3)
    => {:end pointer
        :cube->tweeners cube->tweeners
        :edge? false
        :events [:event-2]}

    (leu/set-flee :unit-1) => :unit-2

    (tsu/move-unit {:game/cube :cube-1
                    :game/battlefield {:cube-1 :unit-2}
                    :game/charge {:charger :cube-2}}
                   :cube-1
                   pointer)
    => {:game/events [:event-1]
        :game/charge {:target-keys #{:unit-key-1}
                      :target-cubes #{:cube-1}}}

    (tsb/reset-battlemap {:game/phase [:charge :reaction :flee :roll]
                          :game/events [:event-1 :event-2]
                          :game/charge {:target-keys #{}
                                        :target-cubes #{}
                                        :edge? false
                                        :unit :unit-1
                                        :roll [1 2]}}
                         [:cube-2])
    => {:game/battlemap {:cube-2 :mover-2}}

    (tb/set-presentation {:cube-1 :mover-1
                          :cube-2 :mover-2}
                         [:cube-2 :cube-3]
                         :marked)
    => :battlemap-3)))


(facts
 "fleeing"

 (fleeing :state-1)
 => :flee

 (provided
  (flee :state-1) => :flee))


(facts
 "reset reaction"

 (let [state {:game/units :units-1
              :game/charge {:charger :cube-3
                            :target-keys #{:unit-key-1 :unit-key-2}}}]

   (reset-reaction state)
   => :unselect

   (provided
    (tu/get-unit :units-1 :unit-key-1) => :cube-1
    (tu/get-unit :units-1 :unit-key-2) => nil

    (unselect {:game/units :units-1
               :game/charge {:charger :cube-3
                             :target-keys #{:unit-key-1}
                             :target-cubes #{:cube-1}}})
    => :unselect)))


(facts
 "trigger"

 (trigger :state-1)
 => :trigger

 (provided
  (ce/trigger :state-1 reset-reaction) => :trigger))
