(ns hexenhammer.view.entity
  (:require [hexenhammer.view.svg :as svg]))


(defmulti render :entity/class)


(defn render-terrain
  "Generates a base, selectable svg terrain entity"
  [terrain]
  (cond-> (-> (svg/hexagon)
              (svg/add-classes ["terrain" (name (:terrain/type terrain))]))

    (= :selectable (:entity/state terrain))
    (svg/add-classes ["selectable"])

    (= :selected (:entity/state terrain))
    (svg/add-classes ["selected"])

    (= :marked (:entity/state terrain))
    (svg/add-classes ["marked"])))


(defn render-floor
  "Given an object on terrain, renders the terrain it is placed on
  with its :entity/state"
  [object]
  (-> (merge (:object/terrain object)
             (select-keys object [:entity/state]))
      (render-terrain)))


(defn if-selectable
  "Depending on the state, adds a selectable wrapper"
  [element entity]
  (cond-> element

    (#{:selectable :silent-selectable :selected} (:entity/state entity))
    (svg/selectable (:entity/cube entity))))


(defmethod render :terrain
  [terrain]
  (-> (render-terrain terrain)
      (svg/translate (:entity/cube terrain))
      (if-selectable terrain)))


(defmethod render :unit
  [unit]
  (let [int->roman ["0" "i" "ii" "iii" "iv" "v" "vi" "vii" "viii" "ix" "x"]]
    (-> [:g {}
         (render-floor unit)
         (-> [:g {}
              (-> (svg/hexagon)
                  (svg/add-classes ["unit" (str "player-" (:unit/player unit))]))
              (svg/chevron (:unit/facing unit))
              (svg/text (:entity/name unit) -1)
              (svg/text (format "%d x %d" (:unit/F unit) (:unit/R unit)) 0)
              (svg/text (int->roman (:unit/id unit)) 2)]
             (svg/scale 9/10))]

        (svg/translate (:entity/cube unit))
        (if-selectable unit))))


(defmethod render :mover
  [mover]
  (-> [:g {}
       (render-floor mover)
       (-> [:g {}
            (-> (svg/hexagon)
                (svg/add-classes ["mover"
                                  (name (:mover/state mover))
                                  (str "player-" (:unit/player mover))]))

            (for [option (disj (:mover/options mover)
                               (:mover/selected mover)
                               (:mover/highlighted mover))]
              (-> (svg/arrow option)
                  (svg/add-classes ["arrow"])
                  (svg/movable (:entity/cube mover) option)))
            (when-let [selected (:mover/selected mover)]
              (-> (svg/arrow selected)
                  (svg/add-classes ["arrow" "selected"])))
            (when-let [highlighted (:mover/highlighted mover)]
              (cond-> (-> (svg/arrow highlighted)
                          (svg/add-classes ["arrow" "highlighted"]))

                (contains? (:mover/options mover) highlighted)
                (svg/movable (:entity/cube mover) highlighted)))]

           (svg/scale 9/10))]

      (svg/translate (:entity/cube mover))
      (if-selectable mover)))
