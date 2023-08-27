(ns hexenhammer.logic.charge
  (:require [hexenhammer.model.cube :as mc]
            [hexenhammer.logic.core :as l]))


(defn show-charge
  [battlefield cube]
  (->> (for [cube (l/field-of-view battlefield cube)]
         [cube (assoc (battlefield cube) :entity/state :marked)])
       (into {})))
