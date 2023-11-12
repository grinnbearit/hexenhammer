(ns hexenhammer.logic.battlefield.event
  (:require [hexenhammer.logic.cube :as lc]
            [hexenhammer.logic.entity.unit :as leu]
            [hexenhammer.logic.entity.event :as lev]
            [hexenhammer.logic.battlefield.unit :as lbu]))


(defn nearby-friend-annihilated
  "Returns a list of panic events for all player units within 2 hexes of source"
  [battlefield source-cube player]
  (for [cube (conj (lc/neighbours-within source-cube 2) source-cube)
        :when (contains? battlefield cube)
        :let [entity (battlefield cube)]
        :when (and (leu/unit? entity)
                   (leu/friendly? entity player)
                   (lbu/panickable? battlefield cube))]
    (lev/panic (leu/unit-key entity))))
