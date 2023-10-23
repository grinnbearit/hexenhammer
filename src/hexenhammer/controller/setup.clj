(ns hexenhammer.controller.setup
  (:require [hexenhammer.transition.core :as t]))


(defn select-hex
  [state cube]
  (-> (assoc state
             :game/phase [:setup :add-unit]
             :game/selected cube)
      (t/reset-battlemap [cube])
      (update :game/battlemap t/set-presentation [cube] :selected)))


(defn add-unit
  [state cube]
  (-> (assoc state :game/phase [:setup :select-hex])
      (dissoc :game/selected)
      (t/reset-battlemap)
      (update :game/battlemap t/set-presentation :silent-selectable)))
