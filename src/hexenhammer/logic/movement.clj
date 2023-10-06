(ns hexenhammer.logic.movement
  (:require [hexenhammer.logic.core :as l]
            [hexenhammer.logic.unit :as lu]
            [hexenhammer.logic.entity :as le]
            [hexenhammer.logic.terrain :as lt]
            [hexenhammer.model.entity :as me]
            [hexenhammer.model.event :as mv]
            [hexenhammer.model.core :as m]
            [hexenhammer.model.cube :as mc]
            [hexenhammer.model.unit :as mu]
            [clojure.set :as set]))


(defn movable?
  [battlefield cube]
  (let [unit (battlefield cube)]
    (not (or (lu/battlefield-engaged? battlefield cube)
             (get-in unit [:unit/flags :fleeing?])))))


(defn valid-move?
  "Returns true if this pointer can be moved to"
  [battlefield cube pointer]
  (let [shadow-battlefield (lu/remove-unit battlefield cube)]
    (lt/passable? (shadow-battlefield (:cube pointer)))))


(defn valid-end?
  "Returns true if this pointer can be the end step in a move"
  [battlefield cube pointer]
  (let [shadow-battlefield (lu/move-unit battlefield cube pointer)]
    (not (lu/battlefield-engaged? shadow-battlefield (:cube pointer)))))


(defn reform-paths
  "Returns all reform paths, each consisting of two steps"
  [battlefield start]
  (for [facing (disj #{:n :ne :se :s :sw :nw} (:facing start))
        :let [end (mc/->Pointer (:cube start) facing)]
        :when (valid-end? battlefield (:cube start) end)]
    [start end]))


(defn forward-step
  "Given a pointer, returns a set of pointers reachable in a single forward step
  The order of pointers in the list determines the priority"
  [pointer]
  (let [facing->pivots {:n [:nw :ne] :ne [:n :se] :se [:ne :s]
                        :s [:se :sw] :sw [:s :nw] :nw [:sw :n]}
        [lp rp] (facing->pivots (:facing pointer))
        forward-cube (mc/step (:cube pointer) (:facing pointer))]
    [(mc/->Pointer (:cube pointer) lp)
     (mc/->Pointer (:cube pointer) rp)
     (mc/->Pointer forward-cube (:facing pointer))
     (mc/->Pointer forward-cube lp)
     (mc/->Pointer forward-cube rp)]))


(defn forward-paths
  "Returns all sequences of steps reachable in hexes from the passed pointer,
  does not include duplicate paths to the same pointer
  removes paths that end as engaged
  prioritises by shortest path and the order in `forward-step`"
  [battlefield start hexes]
  (loop [queue (conj (clojure.lang.PersistentQueue/EMPTY) [start])
         paths []
         seen #{start}]

    (if (empty? queue)
      paths

      (let [path (peek queue)
            pointer (peek path)]

        (cond (and (= (inc hexes) (count path))
                   (valid-end? battlefield (:cube start) pointer))
              (recur (pop queue) (conj paths path) seen)

              (= (inc hexes) (count path))
              (recur (pop queue) paths seen)

              :else
              (let [steps (->> (forward-step pointer)
                               (filter #(valid-move? battlefield (:cube start) %))
                               (remove seen))
                    next-queue (->> (map #(conj path %) steps)
                                    (into (pop queue)))
                    next-seen (into seen steps)]

                (if (valid-end? battlefield (:cube start) pointer)
                  (recur next-queue (conj paths path) next-seen)
                  (recur next-queue paths next-seen))))))))


(defn reposition-paths
  "returns all valid repositioning paths given the starting pointer and a number of hexes
  removes paths that end as engaged"
  [battlefield start hexes]
  (->> (for [facing (disj #{:n :ne :se :s :sw :nw} (:facing start))
             :let [steps (->> (iterate #(mc/step % facing) (:cube start))
                              (drop 1)
                              (map #(mc/->Pointer % (:facing start)))
                              (take-while #(valid-move? battlefield (:cube start) %))
                              (take hexes)
                              (cons start)
                              (vec))]
             path-length (range 2 (+ 2 hexes))
             :when (<= path-length (count steps))
             :let [path (subvec steps 0 path-length)]
             :when (valid-end? battlefield (:cube start) (peek path))]
         path)
       (cons [start])))


(defn show-battlemap
  "Given a battlefield, player and path-map returns a new battlemap with the movers representing those paths"
  [battlefield player paths]
  (letfn [(reducer [mover-acc pointer]
            (update mover-acc (:cube pointer) (fnil conj #{}) (:facing pointer)))]

    (->> (for [[cube options] (reduce reducer {} (map peek paths))]
           [cube (-> (me/gen-mover cube player :options options)
                     (lt/swap (battlefield cube)))])
         (into {}))))


(defn compress-path
  "Returns a subset of path containing only the last pointer for every cube in the path
  assumes the last pointer is the end step so excludes it"
  [path]
  (letfn [(reducer [acc pointer]
            (if (= (:cube (peek acc))
                   (:cube pointer))
              (conj (pop acc) pointer)
              (conj acc pointer)))]

    (pop (reduce reducer [] path))))


(defn path->breadcrumbs
  "Given a path, returns the battlemap of breadcrumbs representing it"
  [battlefield battlemap player path]
  (->> (for [pointer (compress-path path)
             :let [cube (:cube pointer)]]
         [cube
          (if-let [mover (battlemap cube)]
            (assoc mover
                   :mover/highlighted (:facing pointer)
                   :mover/state :past)
            (-> (me/gen-mover (:cube pointer) player
                              :highlighted (:facing pointer)
                              :state :past)
                (lt/swap (battlefield cube))))])
       (into {})))


(defn show-breadcrumbs
  "returns a map of pointer->breadcrumbs for all paths"
  [battlefield battlemap player paths]
  (->> (for [path paths]
         [(peek path) (path->breadcrumbs battlefield battlemap player path)])
       (into {})))


(defn pointer-events
  "Returns a list of events triggered when entering the passed pointer
  utility function for path-events, assumes battlefield is a shadow-battlefield"
  [battlefield unit pointer]
  (let [cube (:cube pointer)
        entity (battlefield cube)]

    (cond-> []

      (lt/dangerous? (lt/pickup entity))
      (conj (mv/dangerous cube
                          (:unit/player unit)
                          (:entity/name unit)
                          (:unit/id unit)))

      (and (le/unit? entity)
           (lu/enemies? unit entity)
           (not (get-in entity [:unit/flags :fleeing?])))
      (conj (mv/opportunity-attack cube
                                   (:unit/player unit)
                                   (:entity/name unit)
                                   (:unit/id unit)
                                   (mu/unit-strength entity)))

      (and (le/unit? entity)
           (lu/allies? unit entity)
           (<= 8 (mu/unit-strength unit))
           (lu/panickable? battlefield cube))
      (conj (mv/panic cube
                      (:unit/player entity)
                      (:entity/name entity)
                      (:unit/id entity))))))


(defn path-events
  "Returns a list of events for the passed path"
  [battlefield unit path]
  (let [shadow-battlefield (lu/remove-unit battlefield (:entity/cube unit))]

    (->> (drop 1 path)
         (mapcat #(pointer-events shadow-battlefield unit %)))))


(defn show-events
  "Returns a map of pointer->events for all paths"
  [battlefield unit paths]
  (->> (for [path paths]
         [(peek path) (path-events battlefield unit path)])
       (into {})))


(defn show-reform
  "Given a battlefield and cube, returns
  :battlemap,  cube->mover that the unit can reach when reforming
  :pointer->events, the list of events that trigger with this move"
  [battlefield cube]
  (let [unit (battlefield cube)
        start (mc/->Pointer cube (:unit/facing unit))
        paths (reform-paths battlefield start)
        battlemap (show-battlemap battlefield (:unit/player unit) paths)
        pointer->events (show-events battlefield unit paths)]
    {:battlemap battlemap
     :pointer->events pointer->events}))


(defn show-moves
  "Given a battlefield, cube, hexes and a path-fn returns
  :battlemap, cube->mover that the unit can reach when moving
  :path-map, pointer->cube->path that the unit needs to pass through to reach the pointer"
  [battlefield cube hexes path-fn]
  (let [unit (battlefield cube)
        start (mc/->Pointer cube (:unit/facing unit))
        paths (path-fn battlefield start hexes)
        battlemap (show-battlemap battlefield (:unit/player unit) paths)
        breadcrumbs (show-breadcrumbs battlefield battlemap (:unit/player unit) paths)
        pointer->events (show-events battlefield unit paths)]
    {:battlemap battlemap
     :breadcrumbs breadcrumbs
     :pointer->events pointer->events}))


(defn show-forward
  "Given a battlefield and cube, returns
  :battlemap,  cube->mover that the unit can reach when moving forward
  :breadcrumb, pointer->cube->mover that the unit needs to pass through to reach the pointer
  :pointer->events, the list of events that trigger with this move"
  [battlefield cube]
  (let [M (get-in battlefield [cube :unit/M])
        hexes (m/M->hexes M)]
    (show-moves battlefield cube hexes forward-paths)))


(defn show-reposition
  "Given a battlefield and cube, returns
  :battlemap,  cube->mover that the unit can reach when repositioning
  :breadcrumb, pointer->cube->mover that the unit needs to pass through to reach the pointer
  :pointer->events, the list of events that trigger with this move"
  [battlefield cube]
  (let [M (get-in battlefield [cube :unit/M])
        hexes (m/M->hexes (/ M 2))]
    (show-moves battlefield cube hexes reposition-paths)))


(defn list-threats
  "Returns a list of cubes that 'threaten' this unit on the battlefield
  i.e. enemy units within 3 hexes
  assumes the passed cube is on the battlefield and a unit"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (for [neighbour (mc/neighbours-within cube 3)
          :when (contains? battlefield neighbour)
          :let [entity (battlefield neighbour)]
          :when (and (le/unit? entity)
                     (lu/enemies? unit entity))]
      neighbour)))


(defn show-threats
  "Returns a battlemap of cubes that 'threaten' this unit on the battlefield"
  [battlefield cube]
  (->> (for [threat (list-threats battlefield cube)]
         [threat (assoc (battlefield threat) :entity/state :marked)])
       (into {})))


(defn show-march
  "Given a battlefield and cube, returns
  :battlemap,  cube->mover that the unit can reach when marching
  :breadcrumb, pointer->cube->mover that the unit needs to pass through to reach the pointer
  :threats? whether any threats prevent a free march"
  [battlefield cube]
  (let [M (get-in battlefield [cube :unit/M])
        hexes (m/M->hexes (* M 2))
        moves (show-moves battlefield cube hexes forward-paths)
        threats (show-threats battlefield cube)]
    (-> (update moves :battlemap merge threats)
        (assoc :threats? (not (empty? threats))))))


(defn charge-step
  "Given a pointer, returns a set of pointers reachable in a single charge step
  The order of pointers in the list determines the priority
  guide is the facing of the start pointer and limits possible facings"
  [pointer guide]
  (let [facing->pivots {:n [:nw :ne] :ne [:n :se] :se [:ne :s]
                        :s [:se :sw] :sw [:s :nw] :nw [:sw :n]}
        [lp rp] (facing->pivots (:facing pointer))
        forward-cube (mc/step (:cube pointer) (:facing pointer))]
    (if (= guide (:facing pointer))
      [(mc/->Pointer (:cube pointer) lp)
       (mc/->Pointer (:cube pointer) rp)
       (mc/->Pointer forward-cube (:facing pointer))
       (mc/->Pointer forward-cube lp)
       (mc/->Pointer forward-cube rp)]
      [(mc/->Pointer (:cube pointer) guide)
       (mc/->Pointer forward-cube (:facing pointer))
       (mc/->Pointer forward-cube guide)])))


(defn charge-paths
  "Returns a list of paths->targets with a charge path to every target cube
  always returns the shortest path to each target cube prioritised by the order in `charge-step`
  each shortest path to a target _could_ contain other targets that have shorter engagement paths
  but no path can end engaged to a non target"
  [battlefield start targets]
  (loop [queue (conj (clojure.lang.PersistentQueue/EMPTY) [start])
         charges {}
         seen #{start}
         remaining targets]

    (if (or (empty? queue)
            (empty? remaining))
      charges

      (let [path (peek queue)
            pointer (peek path)
            engaged (-> (lu/move-unit battlefield (:cube start) pointer)
                        (lu/engaged-cubes (:cube pointer))
                        (set))
            steps (->> (charge-step pointer (:facing start))
                       (filter #(valid-move? battlefield (:cube start) %))
                       (remove seen))
            next-queue (->> (map #(conj path %) steps)
                            (into (pop queue)))
            next-seen (into seen steps)]

        (if (and (set/subset? engaged targets)
                 (seq (set/intersection engaged remaining)))
          (recur next-queue
                 (assoc charges path engaged)
                 next-seen
                 (set/difference remaining engaged))
          (recur next-queue
                 charges
                 next-seen
                 remaining))))))


(defn list-targets
  "Returns a set of visible enemies to the passed unit cube"
  [battlefield cube]
  (let [unit (battlefield cube)
        max-charge (m/M->hexes (+ 6 (:unit/M unit)))]
    (->> (for [viewed (lu/field-of-view battlefield cube)
               :let [entity (battlefield viewed)]
               :when (and (le/unit? entity)
                          (lu/enemies? unit entity)
                          (<= (mc/distance cube viewed) max-charge))]
           viewed)
         set)))


(defn charger?
  "Returns true if this unit is movable and has any viable charge targets"
  [battlefield cube]
  (and (movable? battlefield cube)
       (let [unit (battlefield cube)
             start (mc/->Pointer cube (:unit/facing unit))
             targets (list-targets battlefield cube)
             paths (charge-paths battlefield start targets)]
         (not (empty? paths)))))


(defn show-targets
  "Returns a breadcrumbs map that marks targets for each pointer"
  [battlefield paths]
  (->> (for [[path targets] paths]
         [(peek path) (l/show-cubes battlefield targets :marked)])
       (into {})))


(defn target-ranges
  "Given charge paths and a source cube Returns a map of pointer to charge ranges, if multiple targets returns the largest"
  [paths cube]
  (->> (for [[path targets] paths]
         [(peek path) (->> (map #(mc/distance cube %) targets)
                           (apply max))])
       (into {})))


(defn show-charge
  [battlefield cube]
  (let [unit (battlefield cube)
        start (mc/->Pointer cube (:unit/facing unit))
        targets (list-targets battlefield cube)
        paths (charge-paths battlefield start targets)
        battlemap (show-battlemap battlefield (:unit/player unit) (keys paths))
        breadcrumbs (show-breadcrumbs battlefield battlemap (:unit/player unit) (keys paths))
        pointer->events (show-events battlefield unit (keys paths))
        unit-map (l/show-cubes battlefield [cube] :selected)
        target-map (show-targets battlefield paths)
        ranges (target-ranges paths cube)]
    {:battlemap (merge battlemap unit-map)
     :breadcrumbs (merge-with merge breadcrumbs target-map)
     :pointer->events pointer->events
     :ranges ranges}))


(defn flee-direction
  [pointer trigger]
  (if (= (:cube pointer) trigger)
    (:facing pointer)
    ({:n :s :ne :sw :se :nw :s :n :sw :ne :nw :se}
     (mc/direction (:cube pointer) trigger))))


(defn flee-path
  "Given a start pointer and a number of hexes, returns {:path :edge?}
  path contains the pointers the unit moves through
  edge? is true if the unit runs off the battlefield edge"
  [battlefield start hexes]
  (loop [path [start]]
    (let [pointer (peek path)]
      (if (and (<= (inc hexes) (count path))
               (valid-move? battlefield (:cube start) pointer)
               (valid-end? battlefield (:cube start) pointer))
        {:path path :edge? false}
        (let [next-step (mc/pointer-step pointer)]
          (if (contains? battlefield (:cube next-step))
            (recur (conj path next-step))
            {:path path :edge? true}))))))


(defn show-flee-map
  "Given a player and a starting point returns the start mover on a battlemap"
  [battlefield player start]
  (let [cube (:cube start)]
    {cube (-> (me/gen-mover cube player
                            :highlighted (:facing start)
                            :state :past)
              (lt/swap (battlefield cube)))}))


(defn show-flee
  [battlefield unit-cube trigger-cube roll]
  (let [unit (battlefield unit-cube)
        pointer (mc/->Pointer unit-cube (:unit/facing unit))
        hexes (m/M->hexes roll)
        direction (flee-direction pointer trigger-cube)
        start (mc/->Pointer unit-cube direction)
        {:keys [path edge?]} (flee-path battlefield start hexes)
        end (last path)
        battlemap (show-flee-map battlefield (:unit/player unit) start)
        events (path-events battlefield unit path)]
    {:battlemap battlemap
     :end end
     :edge? edge?
     :events events}))
