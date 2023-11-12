(ns hexenhammer.transition.battlefield-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.battlefield.unit :as lbu]
            [hexenhammer.transition.battlefield :refer :all]))


(facts
 "reset phase"

 (reset-phase :battlefield-1 [:cube-1 :cube-2])
 => :battlefield-3

 (provided
  (lbu/reset-phase :battlefield-1 :cube-1) => :battlefield-2
  (lbu/reset-phase :battlefield-2 :cube-2) => :battlefield-3))


(facts
 "reset movement"

 (reset-movement :battlefield-1 [:cube-1 :cube-2])
 => :battlefield-3

 (provided
  (lbu/reset-movement :battlefield-1 :cube-1) => :battlefield-2
  (lbu/reset-movement :battlefield-2 :cube-2) => :battlefield-3))
