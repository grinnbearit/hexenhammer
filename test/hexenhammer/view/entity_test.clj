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
          :unit/player 1})

 => [:g {:translate :cube-1}
     [:terrain {}]
     [:hexagon {:transform "scale(0.90)" :class "unit player-1"}]]

 (provided
  (render-terrain {:entity/class :unit
                   :entity/cube :cube-1
                   :unit/player 1})
  => [:terrain {}]

  (svg/hexagon) => [:hexagon {}]

  (svg/translate [:g {}
                  [:terrain {}]
                  [:hexagon {:transform "scale(0.90)" :class "unit player-1"}]]
                 :cube-1)
  => [:g {:translate :cube-1}
      [:terrain {}]
      [:hexagon {:transform "scale(0.90)" :class "unit player-1"}]])


 (render {:entity/class :unit
          :entity/cube :cube-1
          :unit/player 1
          :entity/interaction :selectable})

 => [:g {:translate :cube-1
         :selectable :cube-1}
     [:terrain {}]
     [:hexagon {:transform "scale(0.90)" :class "unit player-1"}]]

 (provided
  (render-terrain {:entity/class :unit
                   :entity/cube :cube-1
                   :unit/player 1
                   :entity/interaction :selectable})
  => [:terrain {}]

  (svg/hexagon) => [:hexagon {}]

  (svg/translate [:g {}
                  [:terrain {}]
                  [:hexagon {:transform "scale(0.90)" :class "unit player-1"}]]
                 :cube-1)
  => [:g {:translate :cube-1}
      [:terrain {}]
      [:hexagon {:transform "scale(0.90)" :class "unit player-1"}]]

  (svg/selectable [:g {:translate :cube-1}
                   [:terrain {}]
                   [:hexagon {:transform "scale(0.90)" :class "unit player-1"}]]
                  :cube-1)
  => [:g {:translate :cube-1
          :selectable :cube-1}
      [:terrain {}]
      [:hexagon {:transform "scale(0.90)" :class "unit player-1"}]]))
