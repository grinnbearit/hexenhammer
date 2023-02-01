(ns hexenhammer.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.unit :refer :all]))


(facts
 "gen warrior"

 (gen-warrior "i")
 => {:hexenhammer/class :unit
     :unit/name "warrior"
     :unit/id "i"
     :unit/models 12
     :unit/facing :n}


 (gen-warrior "ii" :facing :s)
 => {:hexenhammer/class :unit
     :unit/name "warrior"
     :unit/id "ii"
     :unit/models 12
     :unit/facing :s})
