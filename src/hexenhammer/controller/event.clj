(ns hexenhammer.controller.event
  (:require [hexenhammer.logic.core :as l]
            [hexenhammer.logic.unit :as lu]
            [hexenhammer.controller.unit :as cu]))


(defn panic-trigger
  [state unit]
  (let [enemy (l/enemy-player (:unit/player unit))
        enemy-cubes (cu/unit-cubes state enemy)]
    (if (seq enemy-cubes)
      (lu/panic-trigger (:game/battlefield state) (:entity/cube unit) enemy-cubes)
      (:entity/cube unit))))
