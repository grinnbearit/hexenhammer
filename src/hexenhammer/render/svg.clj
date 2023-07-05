(ns hexenhammer.render.svg
  (:require [hexenhammer.render.internal :as int]
            [hexenhammer.cube :as cube]))


(defn svg-unit
  "Writes unit information on the hex"
  [unit & {:keys [selected? highlighted?] :or {selected? false highlighted? false}}]
  (let [int->roman ["i" "ii" "iii" "iv" "v" "vi" "vii" "viii" "ix" "x"]]

    [:g {}
     (int/svg-hexagon :classes (cond-> ["grass"]
                                 selected? (conj "selected")))
     (int/svg-scale
      9/10
      [:g {}
       (int/svg-hexagon :classes (cond-> ["unit" (str "player-" (:unit/player unit))]
                                   highlighted? (conj "highlighted")))

       (int/svg-text -1 (format "%s" (:unit/name unit)))
       (int/svg-text 0 (format "%d x %d" (:unit/files unit) (:unit/ranks unit)))
       (int/svg-text 2 (format "%s" (int->roman (:unit/id unit))))
       (int/svg-chevron (:unit/facing unit))])]))


(defn svg-terrain
  "Returns a green hex with the coordinates printed"
  [cube & {:keys [selected?] :or {selected? false}}]
  (int/svg-hexagon :classes (if selected? ["grass" "selected"] ["grass"])))
