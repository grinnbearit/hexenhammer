(ns hexenhammer.transition
  (:require [hexenhammer.cube :as cube]
            [hexenhammer.engine.logic :as logic]
            [hexenhammer.engine.component :as component]))


(defn gen-battlefield-cubes
  "Returns a list of cube coordinates for a battlefield of size rows x columns"
  [rows columns]
  (let [hop-right (partial cube/add (cube/->Cube 2 -1 -1))
        hop-down (partial cube/add (cube/->Cube 0 1 -1))]
    (->> (interleave (iterate hop-right (cube/->Cube 0 0 0))
                     (iterate hop-right (cube/->Cube 1 0 -1)))
         (take columns)
         (iterate #(map hop-down %))
         (take rows)
         (apply concat))))


(defn gen-initial-state
  "Returns the initial hexenhammer state given a list of rows and columns"
  [rows columns]
  {:game/phase :setup
   :game/player 1
   :map/rows rows
   :map/columns columns
   :map/battlefield (->> (for [cube (gen-battlefield-cubes rows columns)]
                           [cube (component/gen-grass cube)])
                         (into {}))})


(defn unselect-cube
  [state]
  (dissoc state :map/selected))


(defn select-cube
  [{:keys [map/selected map/battlefield] :as state} cube]
  (if (= selected cube)
    (unselect-cube state)
    (assoc state :map/selected cube)))


(defn add-unit
  [state cube & {:keys [player facing]}]
  (let [new-id (get-in state [:map/players player "infantry" :counter] 0)
        new-unit (component/gen-infantry player new-id cube facing)]
    (-> (unselect-cube state)
        (assoc-in [:map/battlefield cube] new-unit)
        (update-in [:map/players player "infantry" :counter] (fnil inc 0))
        (update-in [:map/players player :cubes] (fnil conj #{}) cube))))


(defn remove-unit
  [state cube]
  (let [{:keys [unit/player]} (get-in state [:map/battlefield cube])]
    (-> (unselect-cube state)
        (assoc-in [:map/battlefield cube] (component/gen-grass cube))
        (update-in [:map/players player :cubes] disj cube))))


(defn to-movement
  [state]
  (letfn [(update-movement-status [battlefield cube]
            (update battlefield cube assoc :unit/status :movable))]

    (let [{:keys [game/player map/battlefield]} state]
      (->> (get-in state [:map/players player :cubes])
           (filter #(logic/movable? battlefield (battlefield %)))
           (reduce update-movement-status battlefield)
           (assoc state :game/phase :movement :map/battlefield)))))
