(ns hexenhammer.view.css
  (:require [garden.core :refer [css]]
            [hexenhammer.view.colour :refer [PALETTE]]))


(def STYLESHEET
  (css
   [:polygon
    [:&.terrain {:fill (PALETTE "dark green 1") :stroke "black"}
     [:&.selectable {:stroke "orange"}]
     [:&.selected {:stroke "yellow"}]
     [:&.marked {:stroke "white"}]]
    [:&.unit
     [:&.player-1 {:fill (PALETTE "dark red berry 2") :stroke "black"}]
     [:&.player-2 {:fill (PALETTE "dark cornflower blue 2") :stroke "black"}]]
    [:&.mover
     [:&.future
      [:&.player-1 {:fill (PALETTE "light red berry 1") :stroke "black"}]
      [:&.player-2 {:fill (PALETTE "light cornflower blue 1") :stroke "black"}]]
     [:&.present
      [:&.player-1 {:fill (PALETTE "dark red berry 2") :stroke "black"}]
      [:&.player-2 {:fill (PALETTE "dark cornflower blue 2") :stroke "black"}]]
     [:&.past
      [:&.player-1 {:fill (PALETTE "light red berry 2") :stroke "black"}]
      [:&.player-2 {:fill (PALETTE "light cornflower blue 2") :stroke "black"}]]]
    [:&.arrow
     [:&.selected {:stroke "yellow" :fill "yellow"}]
     [:&.highlighted {:stroke "orange" :fill "orange"}]]]
   [:table :th :td {:border "1px solid"}]
   [:text
    [:&.dice.passed {:color (PALETTE "dark green 2") :font-size "5em"}]
    [:&.dice.failed {:color (PALETTE "dark red 2") :font-size "5em"}]]))
