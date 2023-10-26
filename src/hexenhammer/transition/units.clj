(ns hexenhammer.transition.units)


(defn unit-cubes
  "Returns all unit cubes for the passed player, if no player is passed returns all unit cubes"
  ([units]
   (concat (unit-cubes units 1) (unit-cubes units 2)))
  ([units player]
   (->> (vals (units player))
        (mapcat (comp vals :cubes)))))


(defn next-id
  "Returns the next id for the passed unit"
  [units player name]
  (inc (get-in units [player name :counter] 0)))


(defn inc-id
  "Updates the counter for the passed unit"
  [units player name]
  (update-in units [player name :counter] (fnil inc 0)))


(defn set-unit
  "Adds a unit to units"
  [units {:keys [unit/player unit/name unit/id]} cube]
  (assoc-in units [player name :cubes id] cube))


(defn get-unit
  "Returns the unit-cube referenced by the unit key
  returns nil if not found"
  [units {:keys [unit/player unit/name unit/id]}]
  (get-in units [player name :cubes id]))


(defn remove-unit
  "Removes the unit-cube referenced by the unit-key"
  [units {:keys [unit/player unit/name unit/id]}]
  (update-in units [player name :cubes] dissoc id))
