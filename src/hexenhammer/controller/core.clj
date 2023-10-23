(ns hexenhammer.controller.core
  (:require [hexenhammer.transition.core :as t]))


(defn to-setup
  [state]
  (-> (assoc state :game/phase [:setup :select-hex])
      (t/reset-battlemap)
      (update :game/battlemap t/set-presentation :silent-selectable)))
