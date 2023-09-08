(ns hexenhammer.view.entity
  (:require [midje.sweet :refer :all]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :refer :all]))


(facts
 "render-terrain"

 (render-terrain {:entity/class :default :terrain/type :open})
 => [:hexagon {:class "terrain open"}]

 (provided
  (svg/hexagon) => [:hexagon {}])


 (render-terrain {:entity/class :terrain
                  :terrain/type :open
                  :entity/state :selectable})
 => [:hexagon {:class "terrain open selectable"}]

 (provided
  (svg/hexagon) => [:hexagon {}])


 (render-terrain {:entity/class :terrain
                  :terrain/type :open
                  :entity/state :selected})
 => [:hexagon {:class "terrain open selected"}]

 (provided
  (svg/hexagon) => [:hexagon {}])


 (render-terrain {:entity/class :terrain
                  :terrain/type :open
                  :entity/state :marked})
 => [:hexagon {:class "terrain open marked"}]

 (provided
  (svg/hexagon) => [:hexagon {}]))


(facts
 "render floor"

 (render-floor {:object/terrain {:entity/class :terrain}
                :entity/state :selected})
 => :render-terrain

 (provided
  (render-terrain {:entity/class :terrain
                   :entity/state :selected})
  => :render-terrain))


(facts
 "if selectable"

 (let [entity {:entity/name "entity"}]

   (if-selectable :element entity) => :element)


 (for [entity-state [:selectable :silent-selectable :selected]]

   (let [entity {:entity/name "entity"
                 :entity/state entity-state
                 :entity/cube :cube-1}]

     (if-selectable :element entity) => :selectable

     (provided
      (svg/selectable :element :cube-1) => :selectable))))


(facts
 "render terrain"

 (let [entity {:entity/class :terrain
               :entity/cube :cube-1}]

   (render entity) => :if-selectable

   (provided
    (render-terrain entity) => [:terrain {}]

    (svg/translate [:terrain {}] :cube-1) => :translate

    (if-selectable :translate entity) => :if-selectable)))


(facts
 "render unit"

 (let [unit {:entity/class :unit
             :entity/cube :cube-1
             :entity/name "unit"

             :unit/facing :facing-1
             :unit/id 1
             :unit/player 1

             :unit/F 4
             :unit/R 3}]

   (render unit) => :if-selectable

   (provided
    (render-floor unit) => :terrain-1

    (svg/hexagon) => [:hexagon {}]

    (svg/chevron :facing-1) => [:chevron :facing-1]

    (svg/text "unit" -1) => [:text "unit" -1]

    (svg/text "4 x 3" 0) => [:text "4 x 3" 0]

    (svg/text "i" 2) => [:text "i" 2]

    (svg/scale [:g {}
                [:hexagon {:class "unit player-1"}]
                [:chevron :facing-1]
                [:text "unit" -1]
                [:text "4 x 3" 0]
                [:text "i" 2]]
               9/10)
    => :scale

    (svg/translate [:g {}
                    :terrain-1
                    :scale]
                   :cube-1)
    => :translate

    (if-selectable :translate unit) => :if-selectable)))


(facts
 "render mover"

 (let [mover {:entity/class :mover
              :entity/cube :cube-1
              :unit/player 1
              :mover/options #{}
              :mover/state :future
              :mover/selected nil
              :mover/highlighted nil}]

   (render mover)
   => :if-selected

   (provided
    (render-floor mover) => :terrain-1

    (svg/hexagon) => [:hexagon {}]

    (svg/scale [:g {}
                [:hexagon {:class "mover future player-1"}]
                []
                nil
                nil]
               9/10)
    => :scale

    (svg/translate [:g {}
                    :terrain-1
                    :scale]
                   :cube-1)
    => :translate

    (if-selectable :translate mover) => :if-selected))


 (let [mover {:entity/class :mover
              :entity/cube :cube-1
              :unit/player 1
              :mover/options #{:n :ne :se}
              :mover/state :past
              :mover/selected :ne
              :mover/highlighted :se}]

   (render mover) => :if-selectable

   (provided
    (render-floor mover) => :terrain-1

    (svg/hexagon) => [:hexagon {}]

    (svg/arrow :n) => [:arrow {} :n]
    (svg/arrow :ne) => [:arrow {} :ne]
    (svg/arrow :se) => [:arrow {} :se]

    (svg/movable [:arrow {:class "arrow"} :n] :cube-1 :n) => [:movable :n]
    (svg/movable [:arrow {:class "arrow highlighted"} :se] :cube-1 :se) => [:movable :se]

    (svg/scale [:g {}
                [:hexagon {:class "mover past player-1"}]
                (list [:movable :n])
                [:arrow {:class "arrow selected"} :ne]
                [:movable :se]]
               9/10)
    => :scale

    (svg/translate [:g {}
                    :terrain-1
                    :scale]
                   :cube-1)
    => :translate

    (if-selectable :translate mover)
    => :if-selectable)))
