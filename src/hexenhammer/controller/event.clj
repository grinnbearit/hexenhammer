(ns hexenhammer.controller.event)


(defn push-phase
  "If not already saved, saves the phase to return to when processing events"
  [state]
  (if (not (contains? state :game/trigger))
    (->> (select-keys state [:game/phase :game/subphase])
         (assoc state :game/trigger))
    state))


(defn event-transition
  "Changes the game phase and updates the :game/trigger object"
  [state event]
  (assoc state :game/phase (:event/class event) :game/subphase :start))


(defn pop-phase
  "If saved, returns the saved phase from the :game/trigger object to the top level and clears :game/trigger"
  [state]
  (if (contains? state :game/trigger)
    (->> (select-keys (:game/trigger state) [:game/phase :game/subphase])
         (merge (dissoc state :game/trigger)))
    state))
