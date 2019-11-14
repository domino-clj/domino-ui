(ns domino-ui.test-page
  (:require
    [domino.core :as domino]
    [domino.ui.core :as core]
    [domino.ui.view]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [domino.ui.component :as component]
    [domino-ui.test-components])
  (:require-macros [domino.core]))

(def default-schema
  {:views   {:default [::component/container
                       {:title "User"}

                       [:div
                        [:label "First name"]
                        [::component/text-input
                         {:id :first-name}]]

                       [:button
                        {:on-click #(rf/dispatch
                                      [::core/update-component-state
                                       :default-ctx
                                       :first-name
                                       (fn [current-state]
                                         (update current-state :disabled? not))])}
                        "toggle first name enabled"]

                       [:div
                        [:label "Last name"]
                        [::component/text-input
                         {:id :last-name}]]

                       [:div
                        [::component/label
                         {:id    :full-name
                          :label "Full name"}]]]}
   :model   [[:demographics
              [:first-name {:id :first-name}]
              [:last-name {:id :last-name}]
              [:full-name {:id :full-name}]]]
   :effects [{:inputs  [:first-name]
              :handler (fn [_ {:keys [first-name]}]
                         (rf/dispatch [::core/update-component-state
                                       :default-ctx
                                       :last-name
                                       {:disabled? (empty? first-name)}]))}]
   :events  [(domino/event [ctx {:keys [first-name last-name]} {:keys [full-name]}]
                           {:full-name (or (when (and first-name last-name)
                                             (str last-name ", " first-name))
                                           first-name
                                           last-name)})]})

(def alt-schema
  {:views  {:default [::component/container
                      {:title "Vitals"}
                      [:div
                       [:label "Height"]
                       [::component/text-input
                        {:id :height}]]

                      [:div
                       [:label "Weight"]
                       [::component/text-input
                        {:id :weight}]]]}
   :model  [[:vital
             [:height {:id :height}]
             [:weight {:id :weight}]
             [:foo {:id :foo}]]]
   :events [(domino/event [ctx {:keys [height weight]} {:keys [foo]}]
                          (when (and height weight)
                            {:foo (* height weight)}))]})

(defn pprint [v]
  (with-out-str (cljs.pprint/pprint v)))

(defn default-ctx-page []
  [:div
   [:h3 "default context"]
   #_[:pre (pprint @(rf/subscribe [::core/ctx :default-ctx]))]
   @(rf/subscribe [::core/view :default-ctx :default])
   [:hr]
   [:label "component states"]
   [:pre (pprint @(rf/subscribe [::core/component-states :default-ctx]))]
   [:label "db state"]
   [:pre (pprint @(rf/subscribe [::core/db :default-ctx]))]])

(defn alternate-ctx-page []
  [:div
   [:h3 "alternate context"]
   @(rf/subscribe [::core/view :alt-ctx :default])
   [:hr]
   [:label "component states"]
   [:pre (pprint @(rf/subscribe [::core/component-states :alt-ctx]))]
   [:label "db state"]
   [:pre (pprint @(rf/subscribe [::core/db :alt-ctx]))]])

(defn home-page []
  [:div
   [default-ctx-page]
   [:hr]
   [alternate-ctx-page]])

(defn mount-root []
  (rf/dispatch-sync [::core/init-ctx :default-ctx default-schema {}])
  (rf/dispatch-sync [::core/init-ctx :alt-ctx alt-schema {}])
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
