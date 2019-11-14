(ns domino.ui.core
  (:require
   [domino.core :as domino]
   [domino.ui.view :as view]
   [re-frame.core :as rf]))

(defn init-context
  "create a new Domino UI context"
  [db [_ ctx-id schema initial-state]]
  (update
    db
    ::contexts
    assoc
    ctx-id
    (-> (domino/initialize schema initial-state)
        (assoc ::views (reduce
                         (fn [views [id view]]
                           (assoc views id (view/render ctx-id view)))
                         {} (:views schema))))))

(rf/reg-event-db ::init-ctx init-context)

(defn merge-component-state
  "merges the current state of a component with the new state"
  [db [_ ctx-id component-id state]]
  (update-in db [::contexts ctx-id ::components component-id]
             (fn [current-state]
               (->> (merge current-state state)
                    (filter second)
                    (into {})))))

(rf/reg-event-db ::merge-component-state update-component-state)

(defn update-component-state
  "update the state of a component using a user provided function that accepts the current state"
  [db [_ ctx-id component-id update-fn]]
  (update-in db [::contexts ctx-id ::components component-id] update-fn))

(rf/reg-event-db ::update-component-state update-component-state)

(defn component-state
  "returns the state of the component specified by the context and the component id"
  [db [_ ctx-id component-id]]
  (get-in db [::contexts ctx-id ::components component-id]))

(rf/reg-sub ::component-state component-state)

(rf/reg-sub
 ::component-states
 (fn [db [_ ctx-id]]
   (get-in db [::contexts ctx-id ::components])))

(rf/reg-sub
 ::ctx
 (fn [db [_ id]]
   (get-in db [::contexts id])))

(rf/reg-sub
 ::model
 (fn [db [_ ctx-id]]
   (get-in db [::contexts ctx-id ::domino/model])))

(rf/reg-sub
 ::views
 (fn [db [_ ctx-id]]
   (get-in db [::contexts ctx-id ::views])))

(rf/reg-sub
 ::view
 (fn [db [_ ctx-id view-id]]
   (get-in db [::contexts ctx-id ::views view-id])))

(rf/reg-sub
 ::db
 (fn [db [_ ctx-id]]
   (get-in db [::contexts ctx-id ::domino/db])))

(defn transact
  "runs a Domino transaction using the value from the specified component as the input"
  [db [_ ctx-id component-id value]]
  (update-in db [::contexts ctx-id]
             domino/transact
             [[(get-in db [::contexts ctx-id ::domino/model :id->path component-id]) value]]))

(rf/reg-event-db ::id transact)

(defn get-value
  "gets the value of the specified component"
  [db [_ ctx-id component-id]]
  (get-in (get-in db [::contexts ctx-id ::domino/db])
          (get-in db [::contexts ctx-id ::domino/model :id->path component-id])))

(rf/reg-sub ::id get-value)

(defn trigger-effects
  "trigger the specified effects"
  [db [_ ctx-id effect-ids]]
  (domino/trigger-effects (get-in db [::contexts ctx-id]) effect-ids))

(rf/reg-event-db
 ::trigger trigger-effects)
