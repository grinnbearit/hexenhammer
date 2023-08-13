(ns hexenhammer.model.logic.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.logic.movement :refer :all]))


(facts
 "reform facings"

 (reform-facings {:cube-1 {:unit/facing :n}} :cube-1)
 => #{:n :sw :nw}

 (provided
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :ne}} :cube-1) => true
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :se}} :cube-1) => true
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :s}} :cube-1) => true
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :sw}} :cube-1) => false
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :nw}} :cube-1) => false
  (mlc/battlefield-engaged? {:cube-1 {:unit/facing :n}} :cube-1) => false))


(facts
 "show reform"

 (let [battlefield {:cube-1 {:unit/player 1 :unit/facing :n}}]

   (show-reform battlefield :cube-1)
   => :mover-1

   (provided
    (reform-facings battlefield :cube-1) => :facings-1

    (me/gen-mover :cube-1 1 :marked :n :options :facings-1 :presentation :selected)
    => :mover-1)))


(facts
 "forward step"

 (forward-step (->Pointer :cube-1 :n))
 => #{(->Pointer :cube-1 :nw)
      (->Pointer :cube-1 :ne)
      (->Pointer :cube-2 :n)
      (->Pointer :cube-2 :nw)
      (->Pointer :cube-2 :ne)}

 (provided
  (mc/step :cube-1 :n)
  => :cube-2))


(facts
 "valid pointer?"

 (valid-pointer? {} (->Pointer :cube-1 :facing-1))
 => false

 (valid-pointer? {:cube-1 {}} (->Pointer :cube-1 :facing-1))
 => false

 (valid-pointer? {:cube-1 {:entity/class :terrain}} (->Pointer :cube-1 :facing-1))
 => true)


(facts
 "forward steps"

 (forward-steps :battlefield #{} :pointer-1 0)
 => [()]


 (forward-steps :battlefield #{} :pointer-1 1)
 => [[:pointer-4]
     [:pointer-2]]

 (provided
  (forward-step :pointer-1) => #{:pointer-2 :pointer-3 :pointer-4}
  (valid-pointer? :battlefield :pointer-2) => true
  (valid-pointer? :battlefield :pointer-3) => false
  (valid-pointer? :battlefield :pointer-4) => true)


 (forward-steps :battlefield #{} :pointer-1 2)
 => [[:pointer-3 :pointer-5]
     [:pointer-2 :pointer-4]]

 (provided
  (forward-step :pointer-1) => #{:pointer-2 :pointer-3}
  (valid-pointer? :battlefield :pointer-2) => true
  (valid-pointer? :battlefield :pointer-3) => true

  (forward-step :pointer-2) => #{:pointer-3 :pointer-4}
  (valid-pointer? :battlefield :pointer-4) => true

  (forward-step :pointer-3) => #{:pointer-5}
  (valid-pointer? :battlefield :pointer-5) => true))


(facts
 "forward paths"

 (let [pointer {:cube :cube-1}]
   (forward-paths {:cube-1 :unit-1} pointer 2)
   => [(list pointer :pointer-2)
       (list pointer :pointer-3)]

   (provided
    (me/gen-terrain :cube-1) => :terrain-1

    (forward-steps {:cube-1 :terrain-1} #{} pointer 2) => [(list :pointer-2)
                                                           (list :pointer-3)])))


(facts
 "M -> hexes"

 (M->hexes 2) => 1
 (M->hexes 3) => 1
 (M->hexes 4) => 1
 (M->hexes 5) => 2)


(facts
 "collect facings"

 (collect-facings [(->Pointer :cube-1 :n)
                   (->Pointer :cube-1 :ne)
                   (->Pointer :cube-2 :n)])
 => {:cube-1 #{:n :ne}
     :cube-2 #{:n}})


(facts
 "show moves"

 (let [battlefield {:cube-1 {:unit/facing :n
                             :unit/M 5
                             :unit/player 1}}]

   (show-moves battlefield :cube-1)
   => {:cube-1 {:entity/cube :cube-1
                :mover/marked :n
                :entity/presentation :selected}
       :cube-2 {:entity/cube :cube-2}}

   (provided
    (M->hexes 5) => 2

    (forward-paths battlefield (->Pointer :cube-1 :n) 2)
    => [[:pointer-1 :pointer-2]
        [:pointer-1 :pointer-3]]

    (collect-facings [:pointer-1 :pointer-2 :pointer-1 :pointer-3])
    => {:cube-1 #{:n :ne}
        :cube-2 #{:n}}

    (me/gen-mover :cube-1 1 :options #{:n :ne}) => {:entity/cube :cube-1}

    (me/gen-mover :cube-2 1 :options #{:n}) => {:entity/cube :cube-2})))
