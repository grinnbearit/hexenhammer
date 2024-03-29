(ns hexenhammer.logic.entity.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :refer :all]))


(facts
 "gen infantry"

 (gen-infantry 1 2 :n 3 4 5)
 => {:entity/class :unit
     :entity/presentation :default
     :entity/los 1

     :unit/player 1
     :unit/name "infantry"
     :unit/id 2
     :unit/facing :n
     :unit/M 3
     :unit/Ld 4
     :unit/W 1
     :unit/F 4
     :unit/R 5
     :unit/model-strength 1
     :unit/ranks 5
     :unit/damage 0})


(facts
 "unit?"

 (unit? {:entity/class :terrain}) => false
 (unit? {:entity/class :unit}) => true)


(facts
 "unit key"

 (unit-key {:unit/player 1
            :unit/name "unit"
            :unit/id 2
            :unit/facing :n})
 => {:unit/player 1
     :unit/name "unit"
     :unit/id 2})


(facts
 "models"

 (models #:unit{:F 3 :ranks 2 :W 2 :damage 0}) => 6
 (models #:unit{:F 3 :ranks 2 :W 2 :damage 1}) => 6
 (models #:unit{:F 3 :ranks 2 :W 2 :damage 2}) => 5
 (models #:unit{:F 3 :ranks 2 :W 2 :damage 3}) => 5
 (models #:unit{:F 3 :ranks 2 :W 2 :damage 4}) => 4
 (models #:unit{:F 3 :ranks 2 :W 2 :damage 5}) => 4
 (models #:unit{:F 3 :ranks 1 :W 2 :damage 0}) => 3)


(facts
 "set-models"

 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 6) => #:unit{:F 3 :ranks 2 :W 2 :damage 0}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 1} 6) => #:unit{:F 3 :ranks 2 :W 2 :damage 0}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 5) => #:unit{:F 3 :ranks 2 :W 2 :damage 2}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 4) => #:unit{:F 3 :ranks 2 :W 2 :damage 4}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 3) => #:unit{:F 3 :ranks 1 :W 2 :damage 0}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 2) => #:unit{:F 3 :ranks 1 :W 2 :damage 2}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 1) => #:unit{:F 3 :ranks 1 :W 2 :damage 4})


(facts
 "destroy models"

 (destroy-models :unit-1 2) => :unit-2

 (provided
  (models :unit-1) => 3

  (set-models :unit-1 1) => :unit-2))


(facts
 "wounds"

 (wounds #:unit{:F 3 :ranks 2 :W 2 :damage 0}) => 12
 (wounds #:unit{:F 3 :ranks 2 :W 2 :damage 1}) => 11
 (wounds #:unit{:F 3 :ranks 2 :W 2 :damage 2}) => 10
 (wounds #:unit{:F 3 :ranks 2 :W 2 :damage 3}) => 9
 (wounds #:unit{:F 3 :ranks 2 :W 2 :damage 4}) => 8
 (wounds #:unit{:F 3 :ranks 2 :W 2 :damage 5}) => 7
 (wounds #:unit{:F 3 :ranks 1 :W 2 :damage 0}) => 6)


(facts
 "set-wounds"

 (set-wounds #:unit{:F 3 :ranks 2 :W 2 :damage 0} 12) => #:unit{:F 3 :ranks 2 :W 2 :damage 0}
 (set-wounds #:unit{:F 3 :ranks 2 :W 2 :damage 0} 11) => #:unit{:F 3 :ranks 2 :W 2 :damage 1}
 (set-wounds #:unit{:F 3 :ranks 2 :W 2 :damage 0} 10) => #:unit{:F 3 :ranks 2 :W 2 :damage 2}
 (set-wounds #:unit{:F 3 :ranks 2 :W 2 :damage 0} 9) => #:unit{:F 3 :ranks 2 :W 2 :damage 3}
 (set-wounds #:unit{:F 3 :ranks 2 :W 2 :damage 0} 8) => #:unit{:F 3 :ranks 2 :W 2 :damage 4}
 (set-wounds #:unit{:F 3 :ranks 2 :W 2 :damage 0} 7) => #:unit{:F 3 :ranks 2 :W 2 :damage 5}
 (set-wounds #:unit{:F 3 :ranks 2 :W 2 :damage 0} 6) => #:unit{:F 3 :ranks 1 :W 2 :damage 0})


(facts
 "damage unit"

 (damage-unit :unit-1 3)
 => :unit-2

 (provided
  (wounds :unit-1) => 4

  (set-wounds :unit-1 1) => :unit-2))


(facts
 "unit strength"

 (let [unit #:unit{:model-strength 1}]

   (unit-strength unit) => 10

   (provided
    (models unit) => 10))


 (let [unit #:unit{:model-strength 2}]

   (unit-strength unit) => 10

   (provided
    (models unit) => 5)))


(facts
 "enemies?"

 (enemies? {:unit/player 1} {:unit/player 1}) => false
 (enemies? {:unit/player 1} {:unit/player 2}) => true)


(facts
 "fleeing?"

 (fleeing? {}) => false
 (fleeing? {:unit/state {:game {:fleeing? true}}}) => true)


(facts
 "panicked?"

 (panicked? {}) => false
 (panicked? {:unit/state {:phase {:panicked? true}}}) => true)


(facts
 "reset phase"

 (let [unit {:entity/class :unit}]

   (reset-phase unit)
   => {:entity/class :unit
       :unit/state {:phase {:strength 10}}}

   (provided
    (unit-strength unit) => 10)))


(facts
 "phase strength"

 (phase-strength {:unit/state {:phase {:strength 10}}}) => 10)


(facts
 "set panicked"

 (set-panicked {}) => {:unit/state {:phase {:panicked? true}}})


(facts
 "set flee"

 (set-flee {}) => {:unit/state {:game {:fleeing? true}
                                :phase {:fled? true}}})


(facts
 "fled?"

 (fled? {}) => false
 (fled? {:unit/state {:phase {:fled? true}}}) => true)


(facts
 "friendly?"

 (friendly? {:unit/player 2} 1) => false
 (friendly? {:unit/player 2} 2) => true)


(facts
 "reset movement"

 (reset-movement {:unit/state {:movement :movement-1}})
 => {:unit/state {}})


(facts
 "set declared"

 (set-declared {}) => {:unit/state {:charge {:declared? true}}})


(facts
 "declared?"

 (declared? {}) => false
 (declared? {:unit/state {:charge {:declared? true}}}) => true)


(facts
 "set marched"

 (set-marched {}) => {:unit/state {:turn {:marched? true}}})


(facts
 "marched?"

 (marched? {}) => false
 (marched? {:unit/state {:turn {:marched? true}}}) => true)
