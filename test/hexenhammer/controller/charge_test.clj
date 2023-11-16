(ns hexenhammer.controller.charge-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.logic.battlefield.movement.charge :as lbmc]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.transition.state.battlemap :as tsb]
            [hexenhammer.controller.charge :refer :all]))


(facts
 "unselect"

 (unselect {:game/charge {:chargers [:cube-1 :cube-2]
                          :cube->enders {}}
            :game/cube :cube-1})
 => {:game/battlemap :battlemap-2}

 (provided
  (tsb/reset-battlemap {:game/charge {:chargers [:cube-1 :cube-2]}
                        :game/phase [:charge :select-hex]}
                       [:cube-1 :cube-2])
  => {:game/battlemap :battlemap-1}

  (tb/set-presentation :battlemap-1 :selectable)
  => :battlemap-2)


 (unselect {:game/charge {:chargers []}
            :game/cube :cube-1
            :game/battlemap :battlemap-1})
 => {:game/charge {:chargers []}
     :game/phase [:charge :to-movement]})


(facts
 "select hex"

 (let [state {:game/battlefield :battlefield-1}]

   (select-hex state :cube-1)
   => {:game/battlemap :battlemap-2}

   (provided
    (lbmc/charge :battlefield-1 :cube-1)
    => {:cube->enders :cube->enders-1
        :pointer->cube->tweeners :pointer->cube->tweeners-1
        :pointer->events :pointer->events-1
        :pointer->targets :pointer->targets-1
        :pointer->range :pointer->range-1}

    (tsb/refresh-battlemap {:game/battlefield :battlefield-1
                            :game/cube :cube-1
                            :game/phase [:charge :pick-targets]
                            :game/battlemap :cube->enders-1
                            :game/charge {:cube->enders :cube->enders-1
                                          :pointer->cube->tweeners :pointer->cube->tweeners-1
                                          :pointer->events :pointer->events-1
                                          :pointer->targets :pointer->targets-1
                                          :pointer->range :pointer->range-1}}
                           [:cube-1])
    => {:game/battlemap :battlemap-1}

    (tb/set-presentation :battlemap-1 [:cube-1] :selected)
    => :battlemap-2)))


(facts
 "select pick targets"

 (select-pick-targets :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "move pick targets"

 (let [cube->enders {:cube-1 {:entity/class :mover
                              :mover/presentation :future}
                     :cube-2 {:entity/class :mover
                              :mover/presentation :future}}
       pointer (lc/->Pointer :cube-2 :n)
       pointer->cube->tweeners {pointer {:cube-2 {:entity/class :mover
                                                  :mover/presentation :past}
                                         :cube-3 {:entity/class :mover
                                                  :mover/presentation :past}}}
       pointer->events {pointer :events-1}
       pointer->targets {pointer :targets-1}
       pointer->range {pointer :range-1}
       state {:game/charge {:cube->enders cube->enders
                            :pointer->cube->tweeners pointer->cube->tweeners
                            :pointer->events pointer->events
                            :pointer->targets pointer->targets
                            :pointer->range pointer->range}}]

   (move-pick-targets state pointer)
   => {:game/battlemap :battlemap-3}

   (provided
    (tsb/refresh-battlemap {:game/pointer pointer
                            :game/phase [:charge :declare-targets]
                            :game/battlemap {:cube-1 {:entity/class :mover
                                                      :mover/presentation :future}
                                             :cube-2 {:entity/class :mover
                                                      :entity/presentation :selected
                                                      :mover/presentation :present
                                                      :mover/selected :n}
                                             :cube-3 {:entity/class :mover
                                                      :mover/presentation :past}}
                            :game/charge {:cube->enders cube->enders
                                          :pointer->cube->tweeners pointer->cube->tweeners
                                          :pointer->events pointer->events
                                          :pointer->targets pointer->targets
                                          :pointer->range pointer->range
                                          :events :events-1
                                          :targets :targets-1
                                          :charge-range :range-1}}
                           :targets-1)
    => {:game/battlemap :battlemap-2}

    (tb/set-presentation :battlemap-2 :targets-1 :marked)
    => :battlemap-3)))


(facts
 "select declare targets"

 (select-declare-targets :state-1 :cube-1)
 => :unselect

 (provided
  (unselect :state-1) => :unselect))


(facts
 "move declare targets"

 (move-declare-targets :state-1 :pointer-1)
 => :move-pick-targets

 (provided
  (move-pick-targets :state-1 :pointer-1) => :move-pick-targets))


(facts
 "skip charge"

 (let [state {:game/cube :cube-1
              :game/battlefield :battlefield-1
              :game/charge {:chargers #{:cube-1 :cube-2}}}]

   (skip-charge state)
   => :unselect

   (provided
    (unselect {:game/cube :cube-1
               :game/battlefield :battlefield-1
               :game/charge {:chargers #{:cube-2}}})
    => :unselect)))
