(ns hexenhammer.view.html-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.view.html :refer :all]))


(facts
 "entity -> z"

 (entity->z {:entity/presentation :default}) => 0
 (entity->z {:entity/presentation :selected}) => 1)
