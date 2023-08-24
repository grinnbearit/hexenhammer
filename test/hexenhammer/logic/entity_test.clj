(ns hexenhammer.logic.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.entity :refer :all]))


(facts
 "unit?"

 (unit? {:entity/class :terrain}) => false
 (unit? {:entity/class :unit}) => true)


(facts
 "terrain?"

 (terrain? {:entity/class :unit}) => false
 (terrain? {:entity/class :terrain}) => true)
