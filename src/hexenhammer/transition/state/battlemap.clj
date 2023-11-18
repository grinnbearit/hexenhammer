(ns hexenhammer.transition.state.battlemap)


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


(defn refill-battlemap
  "Updates the battlemap with cubes pulled from the battlefield IFF the keys
  aren't already present on the battlemap"
  [{:keys [game/battlemap] :as state} cubes]
  (let [missing (remove #(contains? battlemap %) cubes)]
    (refresh-battlemap state missing)))
