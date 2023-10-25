(ns hexenhammer.controller.core
  (:require [hexenhammer.transition.core :as t]
            [hexenhammer.transition.battlemap :as tb]
            [hexenhammer.controller.movement :as cm]))



(defn to-setup
  [state]
  (-> (assoc state :game/phase [:setup :select-hex])
      (t/reset-battlemap)
      (update :game/battlemap tb/set-presentation :silent-selectable)))


(defn to-movement
  [state]
  (-> (assoc state :game/player 1)
      (cm/reset-movement)))
