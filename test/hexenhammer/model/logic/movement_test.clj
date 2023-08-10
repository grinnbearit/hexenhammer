(ns hexenhammer.model.logic.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.logic.movement :refer :all]))


(facts
 "show reform"

 (show-reform {:cube-1 {:unit/facing :n}} :cube-1)
 => #{:n :sw :nw}

 (provided
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :ne}} :cube-1) => true
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :se}} :cube-1) => true
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :s}} :cube-1) => true
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :sw}} :cube-1) => false
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :nw}} :cube-1) => false
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :n}} :cube-1) => false))
