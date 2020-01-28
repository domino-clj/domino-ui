(ns domino.ui.core
  (:require
   [domino.core :as domino]
   [domino.ui.view :as view]
   [re-frame.core :as rf]))

(defn init-context
  "create a new Domino UI context"
  [db [_ context-id schema initial-state]]
  (update
   db
   ::contexts
   assoc
   context-id
   (assoc (domino/initialize schema initial-state)
          ::views (reduce
                   (fn [views [id view]]
                     (assoc views id (view/render context-id view)))
                   {} (:views schema)))))

(rf/reg-event-db ::init-ctx init-context)

(defn merge-component-state
  "merges the current state of a component with the new state"
  [db [_ ctx-id component-id state]]
  (update-in db [::contexts ctx-id ::components component-id]
             (fn [current-state]
               (->> (merge current-state state)
                    (filter second)
                    (into {})))))

(rf/reg-event-db ::merge-component-state merge-component-state)

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

(rf/reg-sub
  ::change-history
  (fn [db [_ ctx-id]]
    (get-in db [::contexts ctx-id ::domino/change-history])))

(rf/reg-event-db
 ::transact-path
 (fn [db [_ ctx-id & more]]
   (let [[opts path-value-pairs] (if (map? (first more))
                                 [(first more) (rest more)]
                                 [nil more])]
     (update-in db [::contexts ctx-id]
                (fn [ctx]
                  (let [result (domino/transact ctx path-value-pairs)]
                    (when-let [on-transact (:on-transact opts)]
                      (on-transact result))
                    result))))))

(defn transact
  "runs a Domino transaction using the value from the specified component as the input"
  [db [_ ctx-id & more]]
  (let [[opts id-value-pairs] (if (map? (first more))
                                [(first more) (rest more)]
                                [nil more])]
    (update-in db [::contexts ctx-id]
               (fn [ctx]
                 (let [result (domino/transact
                               ctx
                               (map
                                (fn [[component-id value]]
                                  [(get-in db [::contexts ctx-id ::domino/model :id->path component-id]) value])
                                id-value-pairs))]
                   (when-let [on-transact (:on-transact opts)]
                     (on-transact result))
                   result)))))

(rf/reg-event-db ::transact transact)


(rf/reg-sub
 ::subscribe-path
 (fn [db [_ ctx-id path]]
   (get-in (get-in db [::contexts ctx-id ::domino/db]) path)))

(defn get-value
  "gets the value of the specified component"
  [db [_ ctx-id component-id]]
  (get-in (get-in db [::contexts ctx-id ::domino/db])
          (get-in db [::contexts ctx-id ::domino/model :id->path component-id])))

(rf/reg-sub ::subscribe get-value)

(defn trigger-effects
  "trigger the specified effects"
  [db [_ ctx-id effect-ids]]
  (update-in db [::contexts ctx-id] domino/trigger-effects effect-ids))

(rf/reg-event-db
 ::trigger trigger-effects)
