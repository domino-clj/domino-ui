(ns ^:figwheel-no-load domino-ui.dev
  (:require
    [domino-ui.test-page :as test-page]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

(test-page/init!)
