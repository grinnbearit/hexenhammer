(ns hexenhammer.controller.battlemap)


(defn refresh-battlemap
  "Pulls the marked cubes from the battlefield into the battlemap"
  [state cubes]
  (->> (select-keys (:game/battlefield state) cubes)
       (update state :game/battlemap merge)))
