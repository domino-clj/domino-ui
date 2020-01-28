(ns domino.ui.view
  (:require
   [domino.ui.component :refer [component]]
   [clojure.walk :refer [prewalk]]
   [re-frame.core :as rf]))

(defn component? [node]
  (and (vector? node)
       (keyword? (first node))
       (= "domino.ui.component" (namespace (first node)))))

(defn render [context-id view]
  (prewalk
   (fn [node]
     (if (component? node)
       [(component (update node 1 assoc :context-id context-id :render render))]
       node))
   view))
