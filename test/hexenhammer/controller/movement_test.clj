(ns hexenhammer.controller.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.controller.movement :refer :all]))


(facts
 "set mover selected"

 (set-mover-selected {} (mc/->Pointer :cube-1 :facing-1))
 => {:cube-1 {:mover/selected :facing-1
              :mover/state :present
              :entity/presentation :selected}})


(facts
 "reset state"

 (reset-state {:game/battlefield :battlefield-1
               :movement/moved? true
               :game/battlemap :battlemap
               :movement/breadcrumbs :breadcrumbs})
 => {:game/battlefield :battlefield-1})