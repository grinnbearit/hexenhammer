(ns hexenhammer.controller.battlefield
  (:require [hexenhammer.model.logic :as logic]))


(defn reset-default
  "Converts all entities on the battlefield to default presentation and interaction"
  [battlefield]
  (letfn [(reset-entity [entity]
            (assoc entity
                   :entity/presentation :default
                   :entity/interaction :default))]

    (update-vals battlefield reset-entity)))


(defn mark-movable
  "Updates the status for all passed unit-cubes
  assumes that all unit-cubes have the same active player"
  [battlefield unit-cubes]
  (letfn [(reducer [battlefield-acc cube]
            (if (not (logic/battlefield-engaged? battlefield-acc cube))
              (update battlefield-acc cube assoc
                      :entity/presentation :highlighted
                      :entity/interaction :selectable)
              battlefield-acc))]

    (reduce reducer battlefield unit-cubes)))
