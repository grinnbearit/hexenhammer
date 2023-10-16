(ns hexenhammer.model.unit-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.unit :refer :all]))


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
 "set-models"

 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 6) => #:unit{:F 3 :ranks 2 :W 2 :damage 0}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 1} 6) => #:unit{:F 3 :ranks 2 :W 2 :damage 0}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 5) => #:unit{:F 3 :ranks 2 :W 2 :damage 2}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 4) => #:unit{:F 3 :ranks 2 :W 2 :damage 4}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 3) => #:unit{:F 3 :ranks 1 :W 2 :damage 0}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 2) => #:unit{:F 3 :ranks 1 :W 2 :damage 2}
 (set-models #:unit{:F 3 :ranks 2 :W 2 :damage 0} 1) => #:unit{:F 3 :ranks 1 :W 2 :damage 4})


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
 "unit key"

 (unit-key {:unit/player 1
            :entity/name "unit"
            :unit/id 2
            :entity/cube :cube-1})
 => {:unit/player 1
     :entity/name "unit"
     :unit/id 2})
