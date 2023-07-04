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
 "show pivots"

 (show-pivots (component/gen-shadow 1 (cube/->Cube 0 0 0) :n))
 => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :nw)
      (component/gen-shadow 1 (cube/->Cube 0 0 0) :ne)})


(facts
 "terrain?"

 (terrain? {:hexenhammer/class :terrain}) => true
 (terrain? {:hexenhammer/class :unit}) => false)


(facts
 "show forward step"

 (let [battlefield (->> (for [q (range -1 2)
                              r (range -1 2)
                              s (range -1 2)
                              :when (zero? (+ q r s))]
                          [(cube/->Cube q r s) {:hexenhammer/class :terrain}])
                        (into {}))]

   (show-forward-step battlefield (component/gen-shadow 1 (cube/->Cube 0 0 0) :n))
   => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 0 0) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :n)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :nw)}

   (show-forward-step battlefield (component/gen-shadow 1 (cube/->Cube 0 -1 1) :n))
   => #{(component/gen-shadow 1 (cube/->Cube 0 -1 1) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :ne)}))


(facts
 "show forward steps"

 (let [battlefield (->> (for [q (range -2 3)
                              r (range -2 3)
                              s (range -2 3)
                              :when (zero? (+ q r s))]
                          [(cube/->Cube q r s) {:hexenhammer/class :terrain}])
                        (into {}))]

   (show-forward-steps battlefield (component/gen-shadow 1 (cube/->Cube 0 0 0) :n) 0)
   => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :n)}

   (show-forward-steps battlefield (component/gen-shadow 1 (cube/->Cube 0 0 0) :n) 1)
   => #{(component/gen-shadow 1 (cube/->Cube 0 0 0) :n)

        (component/gen-shadow 1 (cube/->Cube 0 0 0) :nw)
        (component/gen-shadow 1 (cube/->Cube 0 0 0) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :n)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :ne)
        (component/gen-shadow 1 (cube/->Cube 0 -1 1) :nw)}

   (show-forward-steps battlefield (component/gen-shadow 1 (cube/->Cube 0 0 0) :n) 2)
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

   (show-forward-steps (assoc battlefield (cube/->Cube 0 -1 1) {:hexenhammer/class :unit})
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
 "shadow enemies?"

 (shadow-enemies? (component/gen-infantry 1 1 (cube/->Cube 0 0 0) :n)
                  (component/gen-shadow 1 (cube/->Cube 0 0 0) :n))
 => false


 (shadow-enemies? (component/gen-infantry 2 1 (cube/->Cube 0 0 0) :n)
                  (component/gen-shadow 1 (cube/->Cube 0 0 0) :n))
 => true)


(facts
 "shadow engaged?"

 (shadow-engaged? (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :n)
                  (component/gen-shadow 1 (cube/->Cube 0 0 0) :n))
 => true

 (shadow-engaged? (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :s)
                  (component/gen-shadow 1 (cube/->Cube 0 0 0) :s))
 => true

 (shadow-engaged? (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :n)
                  (component/gen-shadow 1 (cube/->Cube 0 0 0) :s))
 => false

 (shadow-engaged? (component/gen-infantry 1 1 (cube/->Cube 0 -1 1) :s)
                  (component/gen-shadow 1 (cube/->Cube 0 0 0) :n))
 => false)


(facts
 "unit?"

 (unit? {:hexenhammer/class :unit}) => true
 (unit? {:hexenhammer/class :terrain}) => false)


(facts
 "shadow engaged battlefield?"

 (let [cube (cube/->Cube 0 0 0)]

   (shadow-battlefield-engaged? :battlefield {:shadow/position cube})
   => false

   (provided
    (cube/neighbours cube) => [])


   (shadow-battlefield-engaged?  {cube :unit} {:shadow/position cube})
   => false

   (provided
    (cube/neighbours cube)
    => [cube]

    (unit? :unit)
    => false)

   (shadow-battlefield-engaged? {cube :unit} {:shadow/position cube} )
   => true

   (provided
    (cube/neighbours cube)
    => [cube]

    (unit? :unit)
    => true

    (shadow-engaged? :unit {:shadow/position cube})
    => true)))


(facts
 "show move unit forwards"

 (show-move-unit-forwards :battlefield {:unit/M 4 :unit/player 1 :unit/position :cube :unit/facing :n})
 => []

 (provided
  (component/gen-shadow 1 :cube :n) => :shadow
  (show-forward-steps :battlefield :shadow 1) => #{:shadow})

 (show-move-unit-forwards :battlefield {:unit/M 4 :unit/player 1 :unit/position :cube :unit/facing :n})
 => [:step]

 (provided
  (component/gen-shadow 1 :cube :n) => :shadow
  (show-forward-steps :battlefield :shadow 1) => #{:step :shadow}
  (shadow-battlefield-engaged? :battlefield :step) => false))


(facts
 "enemies?"

 (enemies? (component/gen-infantry 1 1 (cube/->Cube 0 0 0) :n)
           (component/gen-infantry 1 2 (cube/->Cube 0 0 0) :n))
 => false


 (enemies? (component/gen-infantry 2 1 (cube/->Cube 0 0 0) :n)
           (component/gen-infantry 1 1 (cube/->Cube 0 0 0) :n))
 => true)


(facts
 "units engaged?"

 (engaged? (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :n)
           (component/gen-infantry 1 1 ( cube/->Cube 0 0 0) :n))
 => true

 (engaged? (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :s)
           (component/gen-infantry 1 1 (cube/->Cube 0 0 0) :s))
 => true

 (engaged? (component/gen-infantry 2 1 (cube/->Cube 0 -1 1) :n)
           (component/gen-infantry 1 1 (cube/->Cube 0 0 0) :s))
 => false

 (engaged? (component/gen-infantry 1 1 (cube/->Cube 0 -1 1) :s)
           (component/gen-infantry 1 2 (cube/->Cube 0 0 0) :n))
 => false)


(facts
 "engaged battlefield?"

 (let [cube (cube/->Cube 0 0 0)]

   (battlefield-engaged? :battlefield {:unit/position cube})
   => false

   (provided
    (cube/neighbours cube) => [])


   (battlefield-engaged? {cube :unit} {:unit/position cube})
   => false

   (provided
    (cube/neighbours cube)
    => [cube]

    (unit? :unit)
    => false)

   (battlefield-engaged? {cube :unit} {:unit/position cube} )
   => true

   (provided
    (cube/neighbours cube)
    => [cube]

    (unit? :unit)
    => true

    (engaged? :unit {:unit/position cube})
    => true)))


(facts
 "movable?"

 (movable? :battlefield :unit)
 => false

 (provided
  (battlefield-engaged? :battlefield :unit) => true)

 (movable? :battlefield :unit)
 => true

 (provided
  (battlefield-engaged? :battlefield :unit) => false))
