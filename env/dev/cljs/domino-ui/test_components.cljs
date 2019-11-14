(ns domino-ui.test-components
  (:require
   [domino.ui.core :as core]
   [domino.ui.component :refer [component] :as c]
   [re-frame.core :as rf]))

(defmethod component ::c/label [[_ {:keys [context label id]}]]
  (fn []
    [:label label " " @(rf/subscribe [::core/id context id])]))

(defmethod component ::c/text-input [[_ {:keys [context id]}]]
  (fn []
    (let [{:keys [disabled?]} @(rf/subscribe [::core/component-state context id])]
      [:input
       {:type      :text
        :disabled  disabled?
        :value     @(rf/subscribe [::core/id context id])
        :on-change #(rf/dispatch [::core/id context id (-> % .-target .-value)])}])))

(defmethod component ::c/container [[_ {:keys [context title render]} & body]]
  (fn []
    (into
     [:div
      [:h3 title]
      (for [component body]
        ^{:key component}
        [render context component])])))
