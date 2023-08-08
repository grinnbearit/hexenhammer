(ns hexenhammer.controller.entity)


(defn reset-default
  "resets the entity's presentation and interaction to default"
  [entity]
  (assoc entity
         :entity/presentation :default
         :entity/interaction :default))


(defn set-interactable
  "resets the entity's presentation and interaction to be interactable"
  [entity]
  (assoc entity
         :entity/presentation :highlighted
         :entity/interaction :selectable))
