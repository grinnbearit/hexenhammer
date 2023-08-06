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


(defn set-interactable
  "marks all passed cubes as highlighted and selectable"
  [battlefield cubes]
  (letfn [(reducer [battlefield-acc cube]
            (update battlefield-acc cube assoc
                    :entity/presentation :highlighted
                    :entity/interaction :selectable))]

    (reduce reducer battlefield cubes)))
