(ns hexenhammer.controller.battlefield
  (:require [hexenhammer.controller.entity :as ce]))


(defn reset-default
  "Converts all entities on the battlefield to default presentation and interaction"
  [battlefield]
  (update-vals battlefield ce/reset-default))


(defn set-interactable
  "marks all passed cubes as highlighted and selectable"
  [battlefield cubes]
  (->> (update-vals (select-keys battlefield cubes) ce/set-interactable)
       (merge battlefield)))
