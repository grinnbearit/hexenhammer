(ns hexenhammer.controller.battlefield)


(defn set-state
  "sets all entities on the battlefield to the passed entity state
  if a list of cubes is passed, sets only those cubes instead"
  ([battlefield state]
   (update-vals battlefield #(assoc % :entity/state state)))
  ([battlefield cubes state]
   (->> (set-state (select-keys battlefield cubes) state)
        (merge battlefield))))
