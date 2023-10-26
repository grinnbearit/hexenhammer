(ns hexenhammer.logic.entity.mover-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity.mover :refer :all]))


(facts
 "gen mover"

 (gen-mover :player-1)
 => {:entity/class :mover
     :entity/presentation :default

     :unit/player :player-1

     :mover/options #{}
     :mover/selected nil
     :mover/highlighted nil
     :mover/presentation :future}


 (gen-mover :player-1
            :options #{:n :s :se}
            :selected :s
            :highlighted :se
            :presentation :presentation-1)
 => {:entity/class :mover
     :entity/presentation :default

     :unit/player :player-1

     :mover/options #{:n :s :se}
     :mover/selected :s
     :mover/highlighted :se
     :mover/presentation :presentation-1})
