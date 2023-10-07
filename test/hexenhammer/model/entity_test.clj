(ns hexenhammer.model.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.entity :refer :all]))


(facts
 "gen open ground"

 (gen-open-ground :cube-1)
 => {:entity/class :terrain
     :entity/name "terrain"
     :entity/cube :cube-1
     :entity/state :default
     :entity/los 0

     :terrain/type :open})


(facts
 "gen dangerous terrain"

 (gen-dangerous-terrain :cube-1)
 => {:entity/class :terrain
     :entity/name "terrain"
     :entity/cube :cube-1
     :entity/state :default
     :entity/los 0

     :terrain/type :dangerous})


(facts
 "gen impassable terrain"

 (gen-impassable-terrain :cube-1)
 => {:entity/class :terrain
     :entity/name "terrain"
     :entity/cube :cube-1
     :entity/state :default
     :entity/los 5

     :terrain/type :impassable})


(facts
 "gen unit"

 (gen-infantry :cube-1 :player-1 :id-1 :facing-1)
 => {:entity/class :unit
     :entity/name "infantry"
     :entity/cube :cube-1
     :entity/state :default
     :entity/los 1

     :unit/player :player-1
     :unit/id :id-1
     :unit/facing :facing-1
     :unit/M 4
     :unit/Ld 7
     :unit/W 1
     :unit/F 4
     :unit/R 4
     :unit/model-strength 1
     :unit/ranks 4
     :unit/damage 0}

 (gen-infantry :cube-1 :player-1 :id-1 :facing-1
               :M 3 :Ld 8 :R 1)
 => {:entity/class :unit
     :entity/name "infantry"
     :entity/cube :cube-1
     :entity/state :default
     :entity/los 1

     :unit/player :player-1
     :unit/facing :facing-1
     :unit/id :id-1
     :unit/M 3
     :unit/Ld 8
     :unit/W 1
     :unit/F 4
     :unit/R 1
     :unit/model-strength 1
     :unit/ranks 1
     :unit/damage 0})


(facts
 "gen mover"

 (gen-mover :cube-1 :player-1)
 => {:entity/class :mover
     :entity/cube :cube-1
     :entity/state :default

     :unit/player :player-1

     :mover/options #{}
     :mover/selected nil
     :mover/highlighted nil
     :mover/state :future}

 (gen-mover :cube-1 :player-1
            :options #{:n :s :se}
            :selected :s
            :highlighted :se
            :state :state-1)
 => {:entity/class :mover
     :entity/cube :cube-1
     :entity/state :default

     :unit/player :player-1

     :mover/options #{:n :s :se}
     :mover/selected :s
     :mover/highlighted :se
     :mover/state :state-1})


(facts
 "gen shadow"

 (gen-shadow :cube-1 :player-1 :facing-1)
 => {:entity/class :unit
     :entity/cube :cube-1

     :unit/player :player-1
     :unit/facing :facing-1})
