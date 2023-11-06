(ns hexenhammer.transition.battlefield
  (:require [hexenhammer.logic.battlefield.unit :as lbu]))


(defn reset-phase
  [battlefield unit-cubes]
  (reduce lbu/reset-phase battlefield unit-cubes))
