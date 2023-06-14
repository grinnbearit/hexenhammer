(ns hexenhammer.engine.component-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.cube :as cube]
            [hexenhammer.engine.component :refer :all]))


(facts
 "gen grass"

 (gen-grass)
 => {:hexenhammer/class :terrain
     :terrain/name "grass"})


(facts
 "gen infantry"

 (gen-infantry 0 1)
 => {:hexenhammer/class :unit
     :unit/player 0
     :unit/name "infantry"
     :unit/id 1
     :unit/files 4
     :unit/ranks 4
     :unit/facing :n
     :unit/M 4}


 (gen-infantry 1 2 :facing :s)
 => {:hexenhammer/class :unit
     :unit/player 1
     :unit/name "infantry"
     :unit/id 2
     :unit/files 4
     :unit/ranks 4
     :unit/facing :s
     :unit/M 4})


(facts
 "gen shadow"

 (gen-shadow (cube/->Cube 0 0 0) :n)
 => {:hexenhammer/class :shadow
     :shadow/cube (cube/->Cube 0 0 0)
     :shadow/facing :n})
