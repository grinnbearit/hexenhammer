(ns hexenhammer.render.svg
  (:require [hexenhammer.render.internal :as int]
            [hexenhammer.cube :as cube]))


(defn svg-unit
  "Writes unit information on the hex"
  [unit]
  [:g {}
   (int/svg-hexagon :classes ["grass"])
   (int/svg-scale
    9/10
    [:g {}
     (int/svg-hexagon :classes ["unit"])
     (int/svg-text -1 (format "%s" (:unit/name unit)))
     (int/svg-text 0 (format "%s" (:unit/id unit)))
     (int/svg-text 1 (format "(%d)" (:unit/models unit)))
     (int/svg-chevron (:unit/facing unit))])])


(defn svg-terrain
  "Returns a green hex with the coordinates printed"
  [cube & {:keys [selected?] :or {selected? false}}]
  [:g {}
   (int/svg-hexagon :classes (if selected? ["grass" "selected"] ["grass"]))
   (int/svg-coordinates cube)])
