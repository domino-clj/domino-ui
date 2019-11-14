(ns domino.ui.component
  (:require
   [re-frame.core :as rf]))

(defmulti component first)

(defmethod component :default [component]
  (throw (ex-info (str "unsupported component: " (first component)) {:data component})))
