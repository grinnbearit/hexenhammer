(ns hexenhammer.controller.event-test
  (:require [midje.sweet :refer :all]
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
 "trigger event dangerous"

 (trigger-event {} {:event/type :dangerous})
 => {:game/phase [:event :dangerous]})
