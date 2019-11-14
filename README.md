# domino-ui

A UI component library built on top of re-frame and Domino. The library provides a multimethod for declaring UI components along with re-frame events and subscriptions for managing the state of these components.

## Usage

This section will show how to declare and use domino-ui components.

### Creating components

Components are declared using the `domino.ui.component/component` multimethod. As an example, let's
take a look at declaring a text input component. First, we'll need to require the following namespaces:

```clojure
(ns myapp.components
  (:require
   [domino.ui.core :as core]
   [domino.ui.component :refer [component] :as cp]
   [re-frame.core :as rf]))
```

The component accepts a Hiccup style vector that contains a namespaced keyword specifying the type of the component, followed by
an options map, and an optional body of the component. A declaration for a text input might look as follows:

```clojure
[:domino.ui.component/text-input {:id :first-name}]
```

The `:id` key specified in the options map references the ID for the path in the Domino model. Given the above declaration
we will write the following multimethod to instantiate the component:

```clojure
(defmethod component :domino.ui.component/text-input [[_ {:keys [context id]}]]
  (fn []
    (let [{:keys [disabled?]} @(rf/subscribe [::core/component-state context id])]
      [:input
       {:type      :text
        :disabled  disabled?
        :value     @(rf/subscribe [::core/id context id])
        :on-change #(rf/dispatch [::core/id context id (-> % .-target .-value)])}])))   
```

Note that domino-ui supports multiple Domino contexts, and the context for the specific component will be injected
in the component options map when components are parsed.

The multimethod will receive the vector declaration for the component and create a Reagent component function using it.
The component can observe its state using the `:domino.ui.core/component-state` subscription. This subscription should
contain a map with the state of the component. In the example, the map can have a `:disabled?` key that toggles whether
the component is disabled.

The state of the component can be modified using the `:domino.ui.core/merge-component-state` and `:domino.ui.core/update-component-state`
events. The merge event accepts Domino context, component id, and a map containing the new state that will be merged on top of the current state.
The update event accepts the Domino context, component id, and a function that should accept the current state of the component and return an updated one.

The value of the component is read using the `:domino.ui.core/id` subscription and passing it the Domino context and the component id matching one
of the components specified in the options map. In this case, the id is `:first-name`.

Finally, the component updates the current value in the model associated with the component using the `:domino.ui.core/id` event.
This event accepts the Domino context followed by the value.

Once the component is declared we can create a Domino schema and add a `:views` key to it. This key will contain a map of view
declarations, e.g:

```clojure
(def default-schema
    {:views   {:default [:dv
                         [:h3 "User"]
                         [:div
                          [:label "First name"]
                          [:domino.ui.component/text-input {:id :first-name}]]
                         [:div
                          [:label "Last name"]
                          [:domino.ui.component/text-input {:id :last-name}]]]}
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
```

The view declaration uses plain Hiccup to declare the scaffolding and uses components for interactive elements.
The schema can now be initialized calling the `:domino.ui.core/init-ctx` re-frame event:

```clojure
(rf/dispatch-sync [:domino.ui.core/init-ctx :default-ctx default-schema {}])
```

Once the view is initialized, it can be used via the `:domino.ui.core/view` subscription:

```clojure
(defn default-ctx-page []
  [:div
   [:h3 "default context"]
   @(rf/subscribe [::core/view :default-ctx :default])])
```

See [here](https://github.com/domino-clj/domino-ui/blob/master/env/dev/cljs/domino-ui/test_page.cljs)
for a complete example of this in action.


## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
