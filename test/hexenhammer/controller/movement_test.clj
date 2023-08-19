(ns hexenhammer.controller.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.controller.movement :refer :all]))


(facts
 "set mover selected"

 (set-mover-selected {} (mc/->Pointer :cube-1 :facing-1))
 => {:cube-1 {:mover/selected :facing-1
              :mover/state :present
              :entity/presentation :selected
              :entity/interaction :selectable}})


(facts
 "show moves"

 (let [battlefield {:cube-1 {:entity/cube :cube-1
                             :unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :n)]

   (show-moves {:movement/moved? true
                :movement/battlemap {:battlemap :battlemap-1}
                :movement/breadcrumbs {pointer {:breadcrumbs :breadcrumbs}}
                :game/battlefield battlefield}
               pointer)
   => {:game/battlemap :battlemap-2
       :movement/selected pointer
       :movement/battlemap {:battlemap :battlemap-1}
       :movement/breadcrumbs {pointer {:breadcrumbs :breadcrumbs}}
       :game/battlefield battlefield}

   (provided
    (set-mover-selected {:battlemap :battlemap-1
                         :breadcrumbs :breadcrumbs}
                        pointer)
    => :battlemap-2))


 (let [battlefield {:cube-1 {:entity/cube :cube-1
                             :unit/facing :n}}
       pointer (mc/->Pointer :cube-1 :s)]

   (show-moves {:movement/battlemap {:battlemap :battlemap-1}
                :movement/breadcrumbs {pointer {:breadcrumbs :breadcrumbs}}
                :game/battlefield battlefield}
               pointer)
   => {:movement/moved? true
       :game/battlemap :battlemap-2
       :movement/selected pointer
       :movement/battlemap {:battlemap :battlemap-1}
       :movement/breadcrumbs {pointer {:breadcrumbs :breadcrumbs}}
       :game/battlefield battlefield}

   (provided
    (set-mover-selected {:battlemap :battlemap-1
                         :breadcrumbs :breadcrumbs}
                        pointer)
    => :battlemap-2)))
