(ns hexenhammer.model.logic.movement
  (:require [hexenhammer.model.logic.core :as mlc]
            [hexenhammer.model.entity :as me]))


(defn show-reform
  "Given a cube, returns a set of allowed facings after a reform,
  includes the original facing"
  [battlefield cube]
  (let [unit (battlefield cube)]
    (set
     (for [facing #{:n :ne :se :s :sw :nw}
           :let [shadow (assoc unit :unit/facing facing)
                 new-battlefield (assoc battlefield cube shadow)]
           :when (not (mlc/battlefield-engaged? new-battlefield cube))]
       facing))))
