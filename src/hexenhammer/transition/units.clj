(ns hexenhammer.transition.units)


(defn unit-cubes
  "Returns all unit cubes for the passed player, if no player is passed returns all unit cubes"
  ([units]
   (concat (unit-cubes units 1) (unit-cubes units 2)))
  ([units player]
   (->> (vals (units player))
        (mapcat (comp vals :cubes)))))
