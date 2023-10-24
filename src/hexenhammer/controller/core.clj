(ns hexenhammer.controller.core
  (:require [hexenhammer.transition.battlemap :as tb]))


(defn to-setup
  [state]
  (-> (assoc state :game/phase [:setup :select-hex])
      (tb/reset-battlemap)
      (update :game/battlemap tb/set-presentation :silent-selectable)))
