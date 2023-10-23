(ns hexenhammer.render.entity-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.render.bit :as rb]
            [hexenhammer.render.svg :as rs]
            [hexenhammer.render.entity :refer :all]))


(facts
 "render base"

 (render-base {:terrain/type :open
               :entity/presentation :default})
 => [:hexagon {:class "terrain open default"}]

 (provided
  (rs/hexagon) => [:hexagon {}]))


(facts
 "render terrain"

 (let [entity {:entity/class :terrain
               :entity/presentation :default}]

   (render entity :phase-1 :cube-1)
   => :if-selectable

   (provided
    (render-base entity) => :render-base

    (rs/translate :render-base :cube-1) => :translate

    (rs/if-selectable :translate :default :phase-1 :cube-1) => :if-selectable)))


(facts
 "render unit"

 (let [unit {:entity/class :unit
             :entity/presentation :default

             :unit/terrain {:entity/class :terrain}

             :unit/player 1
             :unit/name "unit"
             :unit/id 1
             :unit/facing :facing-1

             :unit/F 4
             :unit/ranks 3
             :unit/damage 0}]

   (render unit :phase-1 :cube-1) => :if-selectable

   (provided
    (render-base {:entity/class :terrain :entity/presentation :default}) => :terrain-1

    (rs/hexagon) => [:hexagon {}]

    (rs/chevron :facing-1) => [:chevron :facing-1]

    (rs/text "unit" -1) => [:text "unit" -1]

    (rs/text "4 x 3" 0) => [:text "4 x 3" 0]

    (rb/int->roman 1) => "i"

    (rs/text "i" 2) => [:text "i" 2]

    (rs/scale [:g {}
               [:hexagon {:class "unit player-1"}]
               [:chevron :facing-1]
               [:text "unit" -1]
               [:text "4 x 3" 0]
               nil
               [:text "i" 2]]
              9/10)
    => :scale

    (rs/translate [:g {}
                   :terrain-1
                   :scale]
                  :cube-1)
    => :translate

    (rs/if-selectable :translate :default :phase-1 :cube-1) => :if-selectable))


 (let [unit {:entity/class :unit
             :entity/presentation :default

             :unit/terrain {:entity/class :terrain}

             :unit/player 1
             :unit/name "unit"
             :unit/id 1
             :unit/facing :facing-1

             :unit/F 4
             :unit/ranks 3
             :unit/damage 2}]

   (render unit :phase-1 :cube-1) => :if-selectable

   (provided
    (render-base {:entity/class :terrain :entity/presentation :default}) => :terrain-1

    (rs/hexagon) => [:hexagon {}]

    (rs/chevron :facing-1) => [:chevron :facing-1]

    (rs/text "unit" -1) => [:text "unit" -1]

    (rs/text "4 x 3" 0) => [:text "4 x 3" 0]

    (rs/text "[2]" 1) => [:text "[2]" 1]

    (rs/text "i" 2) => [:text "i" 2]

    (rs/scale [:g {}
               [:hexagon {:class "unit player-1"}]
               [:chevron :facing-1]
               [:text "unit" -1]
               [:text "4 x 3" 0]
               [:text "[2]" 1]
               [:text "i" 2]]
              9/10)
    => :scale

    (rs/translate [:g {}
                   :terrain-1
                   :scale]
                  :cube-1)
    => :translate

    (rs/if-selectable :translate :default :phase-1 :cube-1) => :if-selectable)))
