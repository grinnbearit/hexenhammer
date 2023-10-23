(ns hexenhammer.logic.battlefield.unit
  (:require [hexenhammer.logic.entity.terrain :as let]))


(defn remove-unit
  "Returns a new battlefield with the unit removed from the old cube"
  [battlefield unit-cube]
  (update battlefield unit-cube let/clear))
