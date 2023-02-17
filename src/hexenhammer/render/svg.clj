(ns hexenhammer.render.svg
  (:require [hexenhammer.render.internal :as int]
            [hexenhammer.cube :as cube]))



(defn svg-unit
  "Writes unit information on the hex"
  [unit & {:keys [selected?] :or {selected? false}}]
  (let [int->roman ["i" "ii" "iii" "iv" "v" "vi" "vii" "viii" "ix" "x"]]
    [:g {}
     (int/svg-hexagon :classes (if selected? ["grass" "selected"] ["grass"]))
     (int/svg-scale
      9/10
      [:g {}
       (int/svg-hexagon :classes ["unit" (str "player-" (:unit/player unit))])
       (int/svg-text -1 (format "%s" (:unit/name unit)))
       (int/svg-text 0 (format "%s" (int->roman (:unit/id unit))))
       (int/svg-text 1 (format "(%d)" (:unit/models unit)))
       (int/svg-chevron (:unit/facing unit))])]))


(defn svg-terrain
  "Returns a green hex with the coordinates printed"
  [cube & {:keys [selected?] :or {selected? false}}]
  (int/svg-hexagon :classes (if selected? ["grass" "selected"] ["grass"])))
