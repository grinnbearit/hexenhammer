(ns hexenhammer.controller.dice)


(defn roll-die!
  []
  (inc (rand-int 6)))


(defn roll!
  [n]
  (doall (repeatedly n roll-die!)))
