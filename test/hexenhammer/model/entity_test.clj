(ns hexenhammer.model.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.entity :refer :all]))


(facts
 "gen terrain"

 (gen-terrain :cube)
 => {:hexenhammer/entity :terrain
     :terrain/cube :cube})
