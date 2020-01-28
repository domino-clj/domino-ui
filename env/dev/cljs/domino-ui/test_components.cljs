(ns domino-ui.test-components
  (:require
   [domino.ui.core :as core]
   [domino.ui.component :refer [component] :as c]
   [reagent.core :as r]
   [re-frame.core :as rf]))

(defmethod component ::c/label [[_ {:keys [context-id label id]}]]
  (fn []
    [:label label " " @(rf/subscribe [::core/subscribe context-id id])]))

(defmethod component ::c/text-input [[_ {:keys [context-id id]}]]
  (fn []
    (let [{:keys [disabled?]} @(rf/subscribe [::core/component-state context-id id])]
      [:input
       {:type      :text
        :disabled  disabled?
        :value     @(rf/subscribe [::core/subscribe context-id id])
        :on-change #(rf/dispatch [::core/transact context-id [id (-> % .-target .-value)]])}])))

(defmethod component ::c/container [[_ {:keys [context-id title render]} & body]]
  (fn []
    (into
     [:div
      [:h3 title]
      (for [component body]
        ^{:key component}
        [render context-id component])])))

(defn address [context-id path]
  (r/with-let [city-path (conj path :city)
               value (r/atom @(rf/subscribe [::core/subscribe-path context-id city-path]))]
    [:input
     {:type      :text
      :value     @value
      :on-focus  #(reset! value @(rf/subscribe [::core/subscribe-path context-id city-path]))
      :on-change #(reset! value (-> % .-target .-value))
      :on-blur   #(rf/dispatch [::core/transact-path context-id [city-path @value]])}]))

(defmethod component ::c/addresses [[_ {:keys [context-id id]}]]
  (let [context @(rf/subscribe [::core/ctx context-id])
        path (get-in context [:domino.core/model :id->path id])]
    (fn []
     (into
      [:div
       [:h3 "Addresses"]
       (for [uuid (keys @(rf/subscribe [::core/subscribe context-id id]))]
         ^{:key uuid}
         [address context-id (conj path uuid)])]))))

