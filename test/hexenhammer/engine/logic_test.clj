(ns hexenhammer.engine.logic-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.cube :as cube]
            [hexenhammer.engine.component :as component]
            [hexenhammer.engine.logic :refer :all]))


(facts
 "M->hexes"

 (M->hexes 1) => 0
 (M->hexes 2) => 1
 (M->hexes 3) => 1
 (M->hexes 4) => 1
 (M->hexes 5) => 2)


(facts
 "make pivots"

 (make-pivots (component/gen-shadow 1 (cube/->Cube 0 0 0) :n))
 => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :nw)
      (component/gen-shadow 1 (cube/->Cube 0 0 0) :ne)})


(facts
 "terrain?"

 (terrain? {:hexenhammer/class :terrain}) => true
 (terrain? {:hexenhammer/class :unit}) => false)


(facts
 "make forward step"

 (let [battlefield (->> (for [q (range -1 2)
                              r (range -1 2)
                              s (range -1 2)
                              :when (zero? (+ q r s))]
                          [(cube/->Cube q r s) {:hexenhammer/class :terrain}])
                        (into {}))]

   (make-forward-step battlefield (component/gen-shadow 1 (cube/->Cube 0 0 0) :n))
   => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 0 0) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :n)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :nw)}

   (make-forward-step battlefield (component/gen-shadow 1 (cube/->Cube 0 -1 1) :n))
   => #{(component/gen-shadow 1 (cube/->Cube 0 -1 1) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :ne)}))


(facts
 "make forward steps"

 (let [battlefield (->> (for [q (range -2 3)
                              r (range -2 3)
                              s (range -2 3)
                              :when (zero? (+ q r s))]
                          [(cube/->Cube q r s) {:hexenhammer/class :terrain}])
                        (into {}))]

   (make-forward-steps battlefield (component/gen-shadow 1 (cube/->Cube 0 0 0) :n) 0)
   => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :n)}

   (make-forward-steps battlefield (component/gen-shadow 1 (cube/->Cube 0 0 0) :n) 1)
   => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :n)

        (component/gen-shadow 1 (cube/->Cube 0 0 0) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 0 0) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :n)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :nw)}

   (make-forward-steps battlefield (component/gen-shadow 1 (cube/->Cube 0 0 0) :n) 2)
   => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :n)

        (component/gen-shadow 1 (cube/->Cube 0 0 0) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 0 0) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :n)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :ne)

        (component/gen-shadow 1 (cube/->Cube 0 0 0) :sw)
        (component/gen-shadow 1 (cube/->Cube -1 0 1) :nw)
        (component/gen-shadow 1 (cube/->Cube -1 0 1) :sw)
        (component/gen-shadow 1 (cube/->Cube -1 0 1) :n)

        (component/gen-shadow 1 (cube/->Cube 0 0 0) :se)
        (component/gen-shadow 1 (cube/->Cube 1 -1 0) :ne)
        (component/gen-shadow 1 (cube/->Cube 1 -1 0) :n)
        (component/gen-shadow 1 (cube/->Cube 1 -1 0) :se)

        (component/gen-shadow 1 (cube/->Cube 0 -2 2) :n)
        (component/gen-shadow 1 (cube/->Cube 0 -2 2) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 -2 2) :ne)

        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :sw)
        (component/gen-shadow 1 (cube/->Cube -1 -1 2) :nw)
        (component/gen-shadow 1 (cube/->Cube -1 -1 2) :n)
        (component/gen-shadow 1 (cube/->Cube -1 -1 2) :sw)

        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :se)
        (component/gen-shadow 1 (cube/->Cube 1 -2 1) :ne)
        (component/gen-shadow 1 (cube/->Cube 1 -2 1) :n)
        (component/gen-shadow 1 (cube/->Cube 1 -2 1) :se)}

   (make-forward-steps (assoc battlefield (cube/->Cube 0 -1 1) {:hexenhammer/class :unit})
                       (component/gen-shadow 1 (cube/->Cube 0 0 0) :n) 2)
   => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :n)

        (component/gen-shadow 1 (cube/->Cube 0 0 0) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 0 0) :ne)

        (component/gen-shadow 1 (cube/->Cube 0 0 0) :sw)
        (component/gen-shadow 1 (cube/->Cube -1 0 1) :nw)
        (component/gen-shadow 1 (cube/->Cube -1 0 1) :sw)
        (component/gen-shadow 1 (cube/->Cube -1 0 1) :n)

        (component/gen-shadow 1 (cube/->Cube 0 0 0) :se)
        (component/gen-shadow 1 (cube/->Cube 1 -1 0) :ne)
        (component/gen-shadow 1 (cube/->Cube 1 -1 0) :n)
        (component/gen-shadow 1 (cube/->Cube 1 -1 0) :se)}))


(facts
 "enemies?"

 (enemies? (component/gen-shadow 1 (cube/->Cube 0 0 0) :n)
           (component/gen-infantry 1 1 (cube/->Cube 0 0 0) :n))
 => false


 (enemies? (component/gen-shadow 1 (cube/->Cube 0 0 0) :n)
           (component/gen-infantry 2 1 (cube/->Cube 0 0 0) :n))
 => true)


(facts
 "engaged?"

 (engaged? (component/gen-shadow 1 (cube/->Cube 0 0 0) :n)
           (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :n))
 => true

 (engaged? (component/gen-shadow 1 (cube/->Cube 0 0 0) :s)
           (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :s))
 => true

 (engaged? (component/gen-shadow 1 (cube/->Cube 0 0 0) :s)
           (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :n))
 => false

 (engaged? (component/gen-shadow 1 (cube/->Cube 0 0 0) :n)
           (component/gen-infantry 1 1 (cube/->Cube 0 -1 1) :s))
 => false)


(facts
 "unit?"

 (unit? {:hexenhammer/class :unit}) => true
 (unit? {:hexenhammer/class :terrain}) => false)


(facts
 "engaged battlefield?"

 (let [cube (cube/->Cube 0 0 0)]

   (engaged-battlefield? :battlefield {:shadow/position cube})
   => false

   (provided
    (cube/neighbours cube) => [])


   (engaged-battlefield? {cube :unit} {:shadow/position cube})
   => false

   (provided
    (cube/neighbours cube)
    => [cube]

    (unit? :unit)
    => false)

   (engaged-battlefield? {cube :unit} {:shadow/position cube})
   => true

   (provided
    (cube/neighbours cube)
    => [cube]

    (unit? :unit)
    => true

    (engaged? {:shadow/position cube} :unit)
    => true)))


(facts
 "move unit forwards"

 (move-unit-forwards :battlefield {:unit/M 4 :unit/player 1 :unit/position :cube :unit/facing :n})
 => []

 (provided
  (component/gen-shadow 1 :cube :n) => :shadow
  (make-forward-steps :battlefield :shadow 1) => #{:shadow})

 (move-unit-forwards :battlefield {:unit/M 4 :unit/player 1 :unit/position :cube :unit/facing :n})
 => [:step]

 (provided
  (component/gen-shadow 1 :cube :n) => :shadow
  (make-forward-steps :battlefield :shadow 1) => #{:step :shadow}
  (engaged? :battlefield :step) => false))