(ns hexenhammer.transition.battlemap)


(defn set-presentation
  "sets all entities on the battlemap to the passed presentation
  if a list of cubes is passed, sets only those cubes instead"
  ([battlemap presentation]
   (update-vals battlemap #(assoc % :entity/presentation presentation)))
  ([battlemap cubes presentation]
   (->> (set-presentation (select-keys battlemap cubes) presentation)
        (merge battlemap))))
