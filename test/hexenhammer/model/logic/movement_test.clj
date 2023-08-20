(ns hexenhammer.model.logic.movement-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.logic.entity :as mle]
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
   => {:cube-1 :mover-1}

   (provided
    (reform-facings battlefield :cube-1) => :facings-1

    (me/gen-mover :cube-1 1 :options :facings-1)
    => :mover-1)))


(facts
 "forward step"

 (forward-step (mc/->Pointer :cube-1 :n))
 => [(mc/->Pointer :cube-1 :nw)
     (mc/->Pointer :cube-1 :ne)
     (mc/->Pointer :cube-2 :n)
     (mc/->Pointer :cube-2 :nw)
     (mc/->Pointer :cube-2 :ne)]

 (provided
  (mc/step :cube-1 :n)
  => :cube-2))


(facts
 "valid pointer?"

 (valid-pointer? {} (mc/->Pointer :cube-1 :facing-1))
 => false

 (valid-pointer? {:cube-1 :unit-1} (mc/->Pointer :cube-1 :facing-1))
 => false

 (provided
  (mle/terrain? :unit-1) => false)

 (valid-pointer? {:cube-1 :terrain-1} (mc/->Pointer :cube-1 :facing-1))
 => true

 (provided
  (mle/terrain? :terrain-1) => true))


(facts
 "forward paths"

 (forward-paths :battlefield :pointer-1 0)
 => [[:pointer-1]]


 (forward-paths :battlefield :pointer-1 1)
 => [[:pointer-1]]

 (provided
  (forward-step :pointer-1) => [])


 (forward-paths :battlefield :pointer-1 1)
 => [[:pointer-1]]

 (provided
  (forward-step :pointer-1) => [:pointer-2]
  (valid-pointer? :battlefield :pointer-2) => false)


 (forward-paths :battlefield :pointer-1 1)
 => [[:pointer-1 :pointer-2]]

 (provided
  (forward-step :pointer-1) => [:pointer-2]
  (valid-pointer? :battlefield :pointer-2) => true)


 (forward-paths :battlefield :pointer-1 2)
 => [[:pointer-1 :pointer-2]]

 (provided
  (forward-step :pointer-1) => [:pointer-2]
  (valid-pointer? :battlefield :pointer-2) => true
  (forward-step :pointer-2) => [:pointer-1]
  (valid-pointer? :battlefield :pointer-1) => true))


(facts
 "reposition paths"

 (let [pointer (mc/->Pointer :cube-1 :n)
       pointer-ne (mc/->Pointer :cube-1-ne :n)
       pointer-se (mc/->Pointer :cube-1-se :n)
       pointer-s (mc/->Pointer :cube-1-s :n)
       pointer-sw (mc/->Pointer :cube-1-sw :n)
       pointer-nw (mc/->Pointer :cube-1-nw :n)]

   (reposition-paths :battlefield pointer 1)
   => (just #{[pointer pointer-ne]
              [pointer pointer-se]
              [pointer pointer-s]
              [pointer pointer-sw]
              [pointer pointer-nw]})

   (provided
    (mc/step :cube-1 :ne) => :cube-1-ne
    (mc/step :cube-1 :se) => :cube-1-se
    (mc/step :cube-1 :s) => :cube-1-s
    (mc/step :cube-1 :sw) => :cube-1-sw
    (mc/step :cube-1 :nw) => :cube-1-nw

    (valid-pointer? :battlefield pointer-ne) => true
    (valid-pointer? :battlefield pointer-se) => true
    (valid-pointer? :battlefield pointer-s) => true
    (valid-pointer? :battlefield pointer-sw) => true
    (valid-pointer? :battlefield pointer-nw) => true)))


(facts
 "paths -> battlemap"

 (paths->battlemap {} 1 [[{:cube :cube-1 :facing :n}]
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

  (me/gen-mover :cube-1 1 :options #{:n :ne}) => :mover-1
  (me/gen-mover :cube-2 1 :options #{:n}) => :mover-2))


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
 "paths -> breadcrumbs"

 (paths->breadcrumbs 1 {:cube-1 {}} [:path-1 :path-2])
 => {:pointer-1 {}
     :pointer-2 {:cube-1 {:mover/highlighted :n
                          :mover/state :past}}
     :pointer-3 {:cube-2 :mover-2}}

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
                             :unit/player 1}}

       path-fn (fn [& args] (identity args))]

   (show-moves battlefield :cube-1 :hexes path-fn)
   => {:battlemap :battlemap-1
       :breadcrumbs :breadcrumbs-1}

   (provided
    (remove-unit battlefield :cube-1) => :new-battlefield

    (paths->battlemap :new-battlefield 1
                      [:new-battlefield (mc/->Pointer :cube-1 :n) :hexes])
    => :battlemap-1

    (paths->breadcrumbs 1 :battlemap-1
                        [:new-battlefield (mc/->Pointer :cube-1 :n) :hexes])
    => :breadcrumbs-1)))


(facts
 "show forward"

 (let [battlefield {:cube-1 {:unit/M 8}}]

   (show-forward battlefield :cube-1)
   => :show-moves

   (provided
    (show-moves battlefield :cube-1 3 forward-paths)
    => :show-moves)))


(facts
 "show reposition"

 (let [battlefield {:cube-1 {:unit/M 8}}]

   (show-reposition battlefield :cube-1)
   => :show-moves

   (provided
    (show-moves battlefield :cube-1 1 reposition-paths)
    => :show-moves)))


(facts
 "show march"

 (let [battlefield {:cube-1 {:unit/M 8}}]

   (show-march battlefield :cube-1)
   => :show-moves

   (provided
    (show-moves battlefield :cube-1 5 forward-paths)
    => :show-moves)))


(facts
 "list threats"

 (list-threats {:cube-1 :unit-1} :cube-1)
 => []

 (provided
  (mc/neighbours-within :cube-1 3)
  => [])


 (list-threats {:cube-1 :unit-1} :cube-1)
 => []

 (provided
  (mc/neighbours-within :cube-1 3)
  => [:cube-2])


 (list-threats {:cube-1 :unit-1
                :cube-2 :terrain-1} :cube-1)
 => []

 (provided
  (mc/neighbours-within :cube-1 3)
  => [:cube-2]

  (mle/unit? :terrain-1)
  => false)


 (list-threats {:cube-1 :unit-1
                :cube-2 :unit-2}
               :cube-1)
 => []

 (provided
  (mc/neighbours-within :cube-1 3)
  => [:cube-2]

  (mle/unit? :unit-2)
  => true

  (mlc/enemies? :unit-1 :unit-2)
  => false)


 (list-threats {:cube-1 :unit-1
                :cube-2 :unit-2}
               :cube-1)
 => [:cube-2]

 (provided
  (mc/neighbours-within :cube-1 3)
  => [:cube-2]

  (mle/unit? :unit-2)
  => true

  (mlc/enemies? :unit-1 :unit-2)
  => true))
