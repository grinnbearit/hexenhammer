(ns hexenhammer.model.logic.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.logic.movement :refer :all]))


(facts
 "pointer -> shadow"

 (pointer->shadow 1 (mc/->Pointer :cube-1 :facing-1))
 => :shadow-1

 (provided
  (me/gen-shadow :cube-1 1 :facing-1) => :shadow-1))


(facts
 "reform facings"

 (reform-facings {:cube-1 {:unit/player 1 :unit/facing :n}} :cube-1)
 => #{:n :sw :nw}

 (provided
  (pointer->shadow 1 (mc/->Pointer :cube-1 :n)) => :shadow-n
  (mlc/battlefield-engaged? {:cube-1 :shadow-n} :cube-1) => false

  (pointer->shadow 1 (mc/->Pointer :cube-1 :ne)) => :shadow-ne
  (mlc/battlefield-engaged? {:cube-1 :shadow-ne} :cube-1) => true

  (pointer->shadow 1 (mc/->Pointer :cube-1 :se)) => :shadow-se
  (mlc/battlefield-engaged? {:cube-1 :shadow-se} :cube-1) => true

  (pointer->shadow 1 (mc/->Pointer :cube-1 :s)) => :shadow-s
  (mlc/battlefield-engaged? {:cube-1 :shadow-s} :cube-1) => true

  (pointer->shadow 1 (mc/->Pointer :cube-1 :sw)) => :shadow-sw
  (mlc/battlefield-engaged? {:cube-1 :shadow-sw} :cube-1) => false

  (pointer->shadow 1 (mc/->Pointer :cube-1 :nw)) => :shadow-nw
  (mlc/battlefield-engaged? {:cube-1 :shadow-nw} :cube-1) => false))


(facts
 "show reform"

 (let [battlefield {:cube-1 {:unit/player 1 :unit/facing :n}}]

   (show-reform battlefield :cube-1)
   => :mover-1

   (provided
    (reform-facings battlefield :cube-1) => :facings-1

    (me/gen-mover :cube-1 1 :selected :n :options :facings-1 :presentation :selected)
    => :mover-1)))


(facts
 "forward step"

 (forward-step (mc/->Pointer :cube-1 :n))
 => #{(mc/->Pointer :cube-1 :nw)
      (mc/->Pointer :cube-1 :ne)
      (mc/->Pointer :cube-2 :n)
      (mc/->Pointer :cube-2 :nw)
      (mc/->Pointer :cube-2 :ne)}

 (provided
  (mc/step :cube-1 :n)
  => :cube-2))


(facts
 "valid pointer?"

 (valid-pointer? {} (mc/->Pointer :cube-1 :facing-1))
 => false

 (valid-pointer? {:cube-1 {}} (mc/->Pointer :cube-1 :facing-1))
 => false

 (valid-pointer? {:cube-1 {:entity/class :terrain}} (mc/->Pointer :cube-1 :facing-1))
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
   (forward-paths :battlefield pointer 2)
   => [(list pointer :pointer-2)
       (list pointer :pointer-3)]

   (provided
    (forward-steps :battlefield #{} pointer 2) => [(list :pointer-2)
                                                   (list :pointer-3)])))


(facts
 "paths -> mover map"

 (paths->mover-map {} 1 [[{:cube :cube-1 :facing :n}]
                         [{:cube :cube-1 :facing :n}
                          {:cube :cube-1 :facing :ne}
                          {:cube :cube-2 :facing :ne}
                          {:cube :cube-2 :facing :n}]])
 => {:cube-1 :mover-1
     :cube-2 :mover-2}

 (provided
  (pointer->shadow 1 {:cube :cube-1 :facing :n}) => :shadow-1
  (mlc/battlefield-engaged? {:cube-1 :shadow-1} :cube-1) => false

  (pointer->shadow 1 {:cube :cube-1 :facing :ne}) => :shadow-2
  (mlc/battlefield-engaged? {:cube-1 :shadow-2} :cube-1) => false

  (pointer->shadow 1 {:cube :cube-2 :facing :ne}) => :shadow-3
  (mlc/battlefield-engaged? {:cube-2 :shadow-3} :cube-2) => true

  (pointer->shadow 1 {:cube :cube-2 :facing :n}) => :shadow-4
  (mlc/battlefield-engaged? {:cube-2 :shadow-4} :cube-2) => false

  (me/gen-mover :cube-1 1 :options #{:n :ne} :state :future) => :mover-1
  (me/gen-mover :cube-2 1 :options #{:n} :state :future) => :mover-2))


(facts
 "compress path"

 (compress-path [{:cube :cube-1 :facing :n}])
 => []

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}])
 => []

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}
                 {:cube :cube-2 :facing :ne}])
 => [{:cube :cube-1 :facing :ne}]

 (compress-path [{:cube :cube-1 :facing :n}
                 {:cube :cube-1 :facing :ne}
                 {:cube :cube-2 :facing :ne}
                 {:cube :cube-2 :facing :n}])
 => [{:cube :cube-1 :facing :ne}])


(facts
 "path -> compressed map"

 (path->compressed-map [:pointer-1 :pointer-2 :pointer-3])
 => {:pointer-1 []
     :pointer-2 []
     :pointer-3 [:pointer-2]}

 (provided
  (compress-path [:pointer-1]) => []
  (compress-path [:pointer-1 :pointer-2]) => []
  (compress-path [:pointer-1 :pointer-2 :pointer-3]) => [:pointer-2]))


(facts
 "paths -> breadcrumbs-map"

 (paths->breadcrumbs-map 1 {:cube-1 {}} [:path-1 :path-2])
 => {:pointer-1 []
     :pointer-2 [{:mover/highlighted :n
                  :mover/state :past}]
     :pointer-3 [:mover-2]}

 (provided
  (path->compressed-map :path-1)
  => {:pointer-1 []
      :pointer-2 [{:cube :cube-1 :facing :n}]}

  (path->compressed-map :path-2)
  => {:pointer-1 []
      :pointer-3 [{:cube :cube-2 :facing :n}]}

  (me/gen-mover :cube-2 1 :highlighted :n :state :past)
  => :mover-2))


(facts
 "M -> hexes"

 (M->hexes 2) => 1
 (M->hexes 3) => 1
 (M->hexes 4) => 1
 (M->hexes 5) => 2)


(facts
 "remove unit"

 (remove-unit {:cube-1 :unit-1} :cube-1)
 => {:cube-1 :terrain}

 (provided
  (me/gen-terrain :cube-1) => :terrain))


(facts
 "show moves"

 (let [battlefield {:cube-1 {:unit/facing :n
                             :unit/M 5
                             :unit/player 1}}]

   (show-moves battlefield :cube-1)
   => {:moves {:cube-1 {:entity/cube :cube-1
                        :mover/selected :n
                        :mover/state :present
                        :entity/presentation :selected}
               :cube-2 {:entity/cube :cube-2}}
       :breadcrumbs :breadcrumbs-map}

   (provided
    (remove-unit battlefield :cube-1) => :new-battlefield

    (M->hexes 5) => 2

    (forward-paths :new-battlefield (mc/->Pointer :cube-1 :n) 2)
    => :paths

    (paths->mover-map :new-battlefield 1 :paths)
    => {:cube-1 {:entity/cube :cube-1}
        :cube-2 {:entity/cube :cube-2}}

    (paths->breadcrumbs-map 1
                            {:cube-1 {:entity/cube :cube-1}
                             :cube-2 {:entity/cube :cube-2}}
                            :paths)
    => :breadcrumbs-map)))
