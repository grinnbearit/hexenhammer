(ns hexenhammer.engine.component-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.cube :as cube]
            [hexenhammer.engine.component :refer :all]))


(facts
 "gen grass"

 (gen-grass (cube/->Cube 0 0 0))
 => {:hexenhammer/class :terrain
     :terrain/name "grass"
     :terrain/position (cube/->Cube 0 0 0)})


(facts
 "gen infantry"

 (gen-infantry 0 1 (cube/->Cube 0 0 0) :n)
 => {:hexenhammer/class :unit
     :unit/player 0
     :unit/name "infantry"
     :unit/id 1
     :unit/files 4
     :unit/ranks 4
     :unit/position (cube/->Cube 0 0 0)
     :unit/facing :n
     :unit/M 4})


(facts
 "gen shadow"

 (gen-shadow 1 (cube/->Cube 0 0 0) :n)
 => {:hexenhammer/class :shadow
     :shadow/player 1
     :shadow/position (cube/->Cube 0 0 0)
     :shadow/facing :n})
