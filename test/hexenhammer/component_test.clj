(ns hexenhammer.component-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.component :refer :all]))


(facts
 "gen grass"

 (gen-grass)
 => {:hexenhammer/class :terrain
     :terrain/name "grass"})


(facts
 "gen warrior"

 (gen-infantry 0 1)
 => {:hexenhammer/class :unit
     :unit/player 0
     :unit/name "infantry"
     :unit/id 1
     :unit/models 12
     :unit/facing :n}


 (gen-infantry 1 2 :facing :s)
 => {:hexenhammer/class :unit
     :unit/player 1
     :unit/name "infantry"
     :unit/id 2
     :unit/models 12
     :unit/facing :s})
