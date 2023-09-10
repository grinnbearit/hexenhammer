(ns hexenhammer.controller.event-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.controller.event :refer :all]))


(facts
 "push phase"

 (push-phase {:game/trigger :trigger-1})
 => {:game/trigger :trigger-1}


 (push-phase {:game/phase :main
              :game/subphase :sub})
 => {:game/phase :main
     :game/subphase :sub
     :game/trigger {:game/phase :main
                    :game/subphase :sub}})


(facts
 "event transition"

 (let [event {:event/class :ec-1}]

   (event-transition {:game/trigger {:game/phase :main}} event)
   => {:game/trigger {:game/phase :main}
       :game/phase :ec-1
       :game/subphase :start}))


(facts
 "pop phase"

 (pop-phase {}) => {}

 (pop-phase {:game/trigger {:game/phase :main
                            :game/subphase :sub}})
 => {:game/phase :main
     :game/subphase :sub})
