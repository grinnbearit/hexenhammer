(ns hexenhammer.model.logic.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.logic.entity :refer :all]))


(facts
 "unit?"

 (unit? {:entity/class :terrain}) => false
 (unit? {:entity/class :unit}) => true)


(facts
 "terrain?"

 (unit? {:entity/class :terrain}) => false
 (unit? {:entity/class :unit}) => true)


(facts
 "place on terrain"

 (onto-terrain {:entity/class :object}
               {:entity/class :terrain
                :entity/state :selected})
 => {:entity/class :object
     :object/terrain {:entity/class :terrain}})
