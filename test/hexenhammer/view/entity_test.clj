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
          :entity/name "unit"
          :unit/player 1
          :unit/id 1
          :unit/facing :n})

 => [:g {:translate :cube-1}
     [:terrain {}]
     [:g {:transform "scale(0.90)"}
      [:hexagon {:class "unit player-1"}]
      [:chevron :n]
      [:text "unit" -1]
      [:text "i" 2]]]

 (provided
  (render-terrain {:entity/class :unit
                   :entity/cube :cube-1
                   :entity/name "unit"
                   :unit/player 1
                   :unit/id 1
                   :unit/facing :n})
  => [:terrain {}]

  (svg/hexagon) => [:hexagon {}]

  (svg/chevron :n) => [:chevron :n]

  (svg/text "unit" -1) => [:text "unit" -1]

  (svg/text "i" 2) => [:text "i" 2]

  (svg/translate [:g {}
                  [:terrain {}]
                  [:g {:transform "scale(0.90)"}
                   [:hexagon {:class "unit player-1"}]
                   [:chevron :n]
                   [:text "unit" -1]
                   [:text "i" 2]]]
                 :cube-1)
  => [:g {:translate :cube-1}
      [:terrain {}]
      [:g {:transform "scale(0.90)"}
       [:hexagon {:class "unit player-1"}]
       [:chevron :n]
       [:text "unit" -1]
       [:text "i" 2]]])


 (render {:entity/class :unit
          :entity/cube :cube-1
          :entity/name "unit"
          :unit/player 1
          :unit/id 1
          :unit/facing :n
          :entity/interaction :selectable})

 => [:g {:translate :cube-1
         :selectable :cube-1}
     [:terrain {}]
     [:g {:transform "scale(0.90)"}
      [:hexagon {:class "unit player-1"}]
      [:chevron :n]
      [:text "unit" -1]
      [:text "i" 2]]]

 (provided
  (render-terrain {:entity/class :unit
                   :entity/cube :cube-1
                   :entity/name "unit"
                   :unit/player 1
                   :unit/id 1
                   :unit/facing :n
                   :entity/interaction :selectable})
  => [:terrain {}]

  (svg/hexagon) => [:hexagon {}]

  (svg/chevron :n) => [:chevron :n]

  (svg/text "unit" -1) => [:text "unit" -1]

  (svg/text "i" 2) => [:text "i" 2]

  (svg/translate [:g {}
                  [:terrain {}]
                  [:g {:transform "scale(0.90)"}
                   [:hexagon {:class "unit player-1"}]
                   [:chevron :n]
                   [:text "unit" -1]
                   [:text "i" 2]]]
                 :cube-1)
  => [:g {:translate :cube-1}
      [:terrain {}]
      [:g {:transform "scale(0.90)"}
       [:hexagon {:class "unit player-1"}]
       [:chevron :n]
       [:text "unit" -1]
       [:text "i" 2]]]

  (svg/selectable [:g {:translate :cube-1}
                   [:terrain {}]
                   [:g {:transform "scale(0.90)"}
                    [:hexagon {:class "unit player-1"}]
                    [:chevron :n]
                    [:text "unit" -1]
                    [:text "i" 2]]]
                  :cube-1)
  => [:g {:translate :cube-1
          :selectable :cube-1}
      [:terrain {}]
      [:g {:transform "scale(0.90)"}
       [:hexagon {:class "unit player-1"}]
       [:chevron :n]
       [:text "unit" -1]
       [:text "i" 2]]]))


(facts
 "render mover"

 (render {:entity/class :mover
          :unit/player 1
          :unit/facing :n
          :entity/cube :cube-1})
 => [:translate :element-2 :cube-1]

 (provided
  (render-terrain {:entity/class :mover
                   :unit/player 1
                   :unit/facing :n
                   :entity/cube :cube-1})
  => :terrain-1

  (svg/hexagon) => [:hexagon {}]

  (svg/arrow :n) => [:arrow {} :n]
  (svg/arrow :ne) => [:arrow {} :ne]
  (svg/arrow :se) => [:arrow {} :se]
  (svg/arrow :s) => [:arrow {} :s]
  (svg/arrow :sw) => [:arrow {} :sw]
  (svg/arrow :nw) => [:arrow {} :nw]

  (svg/scale [:g {}
              [:hexagon {:class "unit player-1"}]
              (list [:arrow {:class "arrow selected"} :n]
                    [:arrow {:class "arrow"} :ne]
                    [:arrow {:class "arrow"} :se]
                    [:arrow {:class "arrow"} :s]
                    [:arrow {:class "arrow"} :sw]
                    [:arrow {:class "arrow"} :nw])]
             9/10)

  => [:scale :element-1 9/10]

  (svg/translate [:g {}
                  :terrain-1
                  [:scale :element-1 9/10]]
                 :cube-1)
  => [:translate :element-2 :cube-1]))
