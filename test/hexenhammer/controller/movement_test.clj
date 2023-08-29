(ns hexenhammer.controller.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.logic.movement :as lm]
            [hexenhammer.controller.movement :refer :all]))


(facts
 "set mover selected"

 (set-mover-selected {} (mc/->Pointer :cube-1 :facing-1))
 => {:cube-1 {:mover/selected :facing-1
              :mover/state :present
              :entity/state :selected}})


(facts
 "show moves"

 (let [battlefield {:cube-1 {:entity/cube :cube-1
                             :unit/player 1
                             :unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)]

   (show-moves {:game/selected :cube-1
                :game/battlefield battlefield
                :game/movement {:moved? true
                                :battlemap {:cube-2 :battlemap-entry}
                                :breadcrumbs {pointer {:cube-3 :breadcrumbs-entry}}}}
               pointer)
   => {:game/selected :cube-1
       :game/battlefield battlefield
       :game/movement {:battlemap {:cube-2 :battlemap-entry}
                       :breadcrumbs {pointer {:cube-3 :breadcrumbs-entry}}
                       :pointer pointer}
       :game/battlemap :battlemap-2}

   (provided
    (set-mover-selected {:cube-2 :battlemap-entry
                         :cube-3 :breadcrumbs-entry}
                        pointer)
    => :battlemap-2))


  (let [battlefield {:cube-1 {:entity/cube :cube-1
                             :unit/player 1
                             :unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :ne)]

   (show-moves {:game/selected :cube-1
                :game/battlefield battlefield
                :game/movement {:moved? true
                                :battlemap {:cube-2 :battlemap-entry}
                                :breadcrumbs {pointer {:cube-3 :breadcrumbs-entry}}}}
               pointer)
   => {:game/selected :cube-1
       :game/battlefield battlefield
       :game/movement {:battlemap {:cube-2 :battlemap-entry}
                       :breadcrumbs {pointer {:cube-3 :breadcrumbs-entry}}
                       :pointer pointer
                       :moved? true}
       :game/battlemap :battlemap-2}

   (provided
    (set-mover-selected {:cube-2 :battlemap-entry
                         :cube-3 :breadcrumbs-entry}
                        pointer)
    => :battlemap-2)))
