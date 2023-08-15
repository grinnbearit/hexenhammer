(ns hexenhammer.view.entity
  (:require [midje.sweet :refer :all]
            [hexenhammer.view.svg :as svg]
            [hexenhammer.view.entity :refer :all]))


(facts
 "render-terrain"

 (render-terrain {})
 => [:hexagon {:class "terrain"}]

 (provided
  (svg/hexagon) => [:hexagon {}])


 (render-terrain {:entity/class :terrain
                  :entity/presentation :selected})
 => [:hexagon {:class "terrain selected"}]

 (provided
  (svg/hexagon) => [:hexagon {}])


 (render-terrain {:entity/class :terrain
                  :entity/presentation :highlighted})
 => [:hexagon {:class "terrain highlighted"}]

 (provided
  (svg/hexagon) => [:hexagon {}]))


(facts
 "render terrain"

 (render {:entity/class :terrain
          :entity/cube :cube-1})
 => [:terrain {:translate :cube-1}]

 (provided
  (render-terrain {:entity/class :terrain
                   :entity/cube :cube-1})
  => [:terrain {}]

  (svg/translate [:terrain {}] :cube-1)
  => [:terrain {:translate :cube-1}])


 (render {:entity/class :terrain
          :entity/cube :cube-1
          :entity/interaction :selectable})

 => [:terrain {:translate :cube-1 :selectable :cube-1}]

 (provided
  (render-terrain {:entity/class :terrain
                   :entity/cube :cube-1
                   :entity/interaction :selectable})
  => [:terrain {}]

  (svg/translate [:terrain {}] :cube-1)
  => [:terrain {:translate :cube-1}]

  (svg/selectable [:terrain {:translate :cube-1}] :cube-1)
  => [:terrain {:translate :cube-1 :selectable :cube-1}]))


(facts
 "render unit"

 (render {:entity/class :unit
          :entity/cube :cube-1
          :unit/facing :facing-1
          :entity/name "unit"
          :unit/id 1
          :unit/player 1})

 => [:transform :element-2 :cube-1]

 (provided
  (render-terrain {:entity/class :unit
                   :entity/cube :cube-1
                   :entity/name "unit"
                   :unit/facing :facing-1
                   :unit/id 1
                   :unit/player 1})
  => :terrain-1

  (svg/hexagon) => [:hexagon {}]

  (svg/chevron :facing-1) => [:chevron :facing-1]

  (svg/text "unit" -1) => [:text "unit" -1]

  (svg/text "i" 2) => [:text "i" 2]

  (svg/scale [:g {}
              [:hexagon {:class "unit player-1"}]
              [:chevron :facing-1]
              [:text "unit" -1]
              [:text "i" 2]]
             9/10)
  => [:scale :element-1 9/10]

  (svg/translate [:g {}
                  :terrain-1
                  [:scale :element-1 9/10]]
                 :cube-1)
  => [:transform :element-2 :cube-1])


 (render {:entity/class :unit
          :entity/cube :cube-1
          :unit/facing :facing-1
          :entity/name "unit"
          :unit/id 1
          :unit/player 1
          :entity/interaction :selectable})

 => [:selectable :element-3 :cube-1]

 (provided
  (render-terrain {:entity/class :unit
                   :entity/cube :cube-1
                   :unit/facing :facing-1
                   :entity/name "unit"
                   :unit/id 1
                   :unit/player 1
                   :entity/interaction :selectable})
  => :terrain-1

  (svg/hexagon) => [:hexagon {}]

  (svg/chevron :facing-1) => [:chevron :facing-1]

  (svg/text "unit" -1) => [:text "unit" -1]

  (svg/text "i" 2) => [:text "i" 2]

  (svg/scale [:g {}
              [:hexagon {:class "unit player-1"}]
              [:chevron :facing-1]
              [:text "unit" -1]
              [:text "i" 2]]
             9/10)
  => [:scale :element-1 9/10]

  (svg/translate [:g {}
                  :terrain-1
                  [:scale :element-1 9/10]]
                 :cube-1)
  => [:translate :element-2 :cube-1]

  (svg/selectable [:translate :element-2 :cube-1] :cube-1)
  => [:selectable :element-3 :cube-1]))


(facts
 "render mover"

 (render {:entity/class :mover
          :entity/cube :cube-1
          :unit/player 1
          :mover/options #{}
          :mover/selected nil})

 => [:translate :element-2 :cube-1]

 (provided
  (render-terrain {:entity/class :mover
                   :entity/cube :cube-1
                   :unit/player 1
                   :mover/options #{}
                   :mover/selected nil})
  => :terrain-1

  (svg/hexagon) => [:hexagon {}]

  (svg/scale [:g {}
              [:hexagon {:class "unit player-1"}]
              []
              nil]
             9/10)
  => [:scale :element-1 9/10]

  (svg/translate [:g {}
                  :terrain-1
                  [:scale :element-1 9/10]]
                 :cube-1)
  => [:translate :element-2 :cube-1])


 (render {:entity/class :mover
          :entity/cube :cube-1
          :unit/player 1
          :mover/options #{:n :s :nw}
          :mover/selected :nw})

 => [:translate :element-2 :cube-1]

 (provided
  (render-terrain {:entity/class :mover
                   :entity/cube :cube-1
                   :unit/player 1
                   :mover/options #{:n :s :nw}
                   :mover/selected :nw})
  => :terrain-1

  (svg/hexagon) => [:hexagon {}]

  (svg/arrow :n) => [:arrow {} :n]
  (svg/arrow :s) => [:arrow {} :s]
  (svg/arrow :nw) => [:arrow {} :n]

  (svg/movable [:arrow {:class "arrow"} :n] :cube-1 :n) => [:movable :n]
  (svg/movable [:arrow {:class "arrow"} :s] :cube-1 :s) => [:movable :s]


  (svg/scale [:g {}
              [:hexagon {:class "unit player-1"}]
              (list [:movable :n]
                    [:movable :s])
              [:arrow {:class "arrow marked"} :n]]
             9/10)
  => [:scale :element-1 9/10]

  (svg/translate [:g {}
                  :terrain-1
                  [:scale :element-1 9/10]]
                 :cube-1)
  => [:translate :element-2 :cube-1]))
