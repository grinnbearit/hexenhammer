(ns hexenhammer.web.css
  (:require [garden.core :refer [css]]
            [hexenhammer.web.colour :refer [PALETTE]]))


(def STYLESHEET
  (css
   [:polygon
    [:&.terrain {:stroke "black"}
     [:&.open {:fill (PALETTE "dark green 1")}]
     [:&.dangerous {:fill (PALETTE "dark orange 1")}]
     [:&.impassable {:fill (PALETTE "dark gray 1")}]
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
   [:table.profile {:text-align "center"}]
   [:text
    [:&.dice {:font-size "5em"}
     [:&.passed {:color (PALETTE "dark green 2")}]
     [:&.failed {:color (PALETTE "dark red 2")}]]]))
