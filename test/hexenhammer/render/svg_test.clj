(ns hexenhammer.render.svg-test
  (:require [midje.sweet :refer :all]
            [hexenhammer.render.svg :refer :all]
            [hexenhammer.render.internal :as int]
            [hexenhammer.cube :as cube]))


(facts
 "svg unit"

 (svg-unit {:unit/player 0
            :unit/name "unit-name"
            :unit/id 0
            :unit/models 12
            :unit/facing :s})
 => [:g {}
     [:hexagon {:class "grass"}]
     [:g {:transform "scale(0.90)"}
      [:hexagon {:class "unit player-0"}]
      [:text -1 "unitname"]
      [:text 0 "i"]
      [:text 1 "(12)"]
      [:chevron :s]]]

 (provided
  (int/svg-hexagon :classes ["grass"]) => [:hexagon {:class "grass"}]
  (int/svg-hexagon :classes ["unit" "player-0"]) => [:hexagon {:class "unit player-0"}]
  (int/svg-text -1 "unit-name") => [:text -1 "unitname"]
  (int/svg-text 0 "i") => [:text 0 "i"]
  (int/svg-text 1 "(12)") => [:text 1 "(12)"]
  (int/svg-chevron :s) => [:chevron :s]))


(facts
 "svg terrain"

 (let [cube (cube/->Cube 0 0 0)]

   (svg-terrain cube)
   => [:hexagon {:class "grass"}]

   (provided
    (int/svg-hexagon :classes ["grass"]) => [:hexagon {:class "grass"}])))
