(ns hexenhammer.view.svg-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.view.unit :refer :all]))


(facts
 "int->roman"

 (int->roman 4) => "iv")
