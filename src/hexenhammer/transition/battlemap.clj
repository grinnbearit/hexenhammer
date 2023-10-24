(ns hexenhammer.transition.battlemap)


(defn reset-battlemap
  "Sets the battlemap to the current battlefield (or a subset of it)"
  ([state]
   (assoc state :game/battlemap (:game/battlefield state)))
  ([state cubes]
   (->> (select-keys (:game/battlefield state) cubes)
        (assoc state :game/battlemap))))


(defn refresh-battlemap
  "Updates the battlemap with cubes pulled from the battlefield"
  [state cubes]
  (->> (select-keys (:game/battlefield state) cubes)
       (update state :game/battlemap merge)))


(defn set-presentation
  "sets all entities on the battlemap to the passed presentation
  if a list of cubes is passed, sets only those cubes instead"
  ([battlemap presentation]
   (update-vals battlemap #(assoc % :entity/presentation presentation)))
  ([battlemap cubes presentation]
   (->> (set-presentation (select-keys battlemap cubes) presentation)
        (merge battlemap))))
