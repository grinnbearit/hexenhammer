(ns hexenhammer.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.unit :refer :all]))


(facts
 "gen warrior"

 (gen-infantry "i")
 => {:hexenhammer/class :unit
     :unit/name "infantry"
     :unit/id "i"
     :unit/models 12
     :unit/facing :n}


 (gen-infantry "ii" :facing :s)
 => {:hexenhammer/class :unit
     :unit/name "infantry"
     :unit/id "ii"
     :unit/models 12
     :unit/facing :s})
