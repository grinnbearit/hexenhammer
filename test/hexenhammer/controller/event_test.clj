(ns hexenhammer.controller.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.dice :as td]
            [hexenhammer.transition.units :as tu]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.units :as tsu]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.event :refer :all]))


(facts
 "trigger"

 (trigger {:game/events []
           :game/battlemap :battlemap-1
           :game/event {:callback identity}})
 => {:game/events []}


 (trigger {:game/events [:event-1]
           :game/battlemap :battlemap-1
           :game/event {:callback :callback-1
                        :data :data-1}})
 => :trigger-event

 (provided
  (trigger-event {:game/events []
                  :game/event {:callback :callback-1}}
                 :event-1)
  => :trigger-event)


 (trigger {:game/events []
           :game/battlemap :battlemap-1}
          identity)
 => {:game/events []})


(facts
 "trigger event dangerous-terrain"

 (let [state {:game/units :units-1}
       event {:event/type :dangerous-terrain
              :event/unit-key :unit-key-1}]

   (trigger-event state event)
   => :trigger

   (provided
    (tu/get-unit :units-1 :unit-key-1) => nil
    (trigger state) => :trigger))


 (let [state {:game/units :units-1
              :game/battlefield {:cube-2 :unit-1}}
       event {:event/type :dangerous-terrain
              :event/cube :cube-1
              :event/unit-key :unit-key-1}]

   (trigger-event state event)
   => {:game/battlemap :battlemap-2}

   (provided
    (tu/get-unit :units-1 :unit-key-1) => :cube-2
    (leu/models :unit-1) => 3
    (td/roll! 3) => :roll
    (td/matches :roll 1) => 3

    (tsu/destroy-unit state :cube-2) => {}

    (tsb/reset-battlemap {:game/phase [:event :dangerous-terrain]
                          :game/event {:models-destroyed 3
                                       :unit-destroyed? true
                                       :roll :roll
                                       :unit :unit-1}}
                         [:cube-1 :cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 :marked) => :battlemap-2))


 (let [state {:game/units :units-1
              :game/battlefield {:cube-2 :unit-1}}
       event {:event/type :dangerous-terrain
              :event/cube :cube-1
              :event/unit-key :unit-key-1}]

   (trigger-event state event)
   => {:game/battlemap :battlemap-2}

   (provided
    (tu/get-unit :units-1 :unit-key-1) => :cube-2
    (leu/models :unit-1) => 4
    (td/roll! 4) => :roll
    (td/matches :roll 1) => 3

    (tsu/destroy-models state :cube-2 :cube-1 3) => {}

    (tsb/reset-battlemap {:game/phase [:event :dangerous-terrain]
                          :game/event {:models-destroyed 3
                                       :unit-destroyed? false
                                       :roll :roll
                                       :unit :unit-1}}
                         [:cube-1 :cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 :marked) => :battlemap-2)))


(facts
 "trigger event heavy casualties"

 (let [state {:game/units :units-1}
       event {:event/type :heavy-casualties
              :event/unit-key :unit-key-1}]

   (trigger-event state event)
   => :trigger

   (provided
    (tu/get-unit :units-1 :unit-key-1) => nil
    (trigger state) => :trigger))


 (let [state {:game/units :units-1
              :game/battlefield :battlefield-1}
       event {:event/type :heavy-casualties
              :event/unit-key :unit-key-1}]

   (trigger-event state event)
   => :trigger

   (provided
    (tu/get-unit :units-1 :unit-key-1) => :cube-2
    (lbu/panickable? :battlefield-1 :cube-2) => false
    (trigger state) => :trigger))


 (let [unit {:unit/Ld 3}
       battlefield {:cube-2 unit}
       state {:game/units :units-1
              :game/battlefield battlefield}
       event {:event/type :heavy-casualties
              :event/cube :cube-1
              :event/unit-key :unit-key-1}]

   (trigger-event state event)
   => {:game/battlemap :battlemap-2}

   (provided
    (tu/get-unit :units-1 :unit-key-1) => :cube-2
    (lbu/panickable? battlefield :cube-2) => true
    (td/roll! 2) => [1 1]
    (leu/set-panicked unit) => :unit-2

    (tsb/reset-battlemap {:game/phase [:event :heavy-casualties :passed]
                          :game/units :units-1
                          :game/battlefield {:cube-2 :unit-2}
                          :game/event {:source-cube :cube-1
                                       :unit-cube :cube-2
                                       :roll [1 1]}}
                         [:cube-1 :cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 :marked)
    => :battlemap-2))


 (let [unit {:unit/Ld 3}
       battlefield {:cube-2 unit}
       state {:game/units :units-1
              :game/battlefield battlefield}
       event {:event/type :heavy-casualties
              :event/cube :cube-1
              :event/unit-key :unit-key-1}]

   (trigger-event state event)
   => {:game/battlemap :battlemap-2}

   (provided
    (tu/get-unit :units-1 :unit-key-1) => :cube-2
    (lbu/panickable? battlefield :cube-2) => true
    (td/roll! 2) => [1 3]
    (leu/set-panicked unit) => :unit-2

    (tsb/reset-battlemap {:game/phase [:event :heavy-casualties :failed]
                          :game/units :units-1
                          :game/battlefield {:cube-2 :unit-2}
                          :game/event {:source-cube :cube-1
                                       :unit-cube :cube-2
                                       :roll [1 3]}}
                         [:cube-1 :cube-2])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 :marked)
    => :battlemap-2)))


(facts
 "flee heavy casualties"

 (flee-heavy-casualties :state)
 => :trigger

 (provided
  (trigger :state) => :trigger))
