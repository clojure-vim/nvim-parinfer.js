(ns nvim-parinfer.change-test
  (:require [cljs.test :refer-macros [deftest is]]
            [nvim-parinfer.core :refer [text-changed]]))

(deftest t-handles-indent-mode
  (is (= (-> (text-changed
              {:parinfer/event "TextChanged"
               :parinfer/cursor [0 0]
               :parinfer/lines ["(a" "(b)"]
               :parinfer/mode "indent"})
           :parinfer/lines)
         ["(a)" "(b)"])))
         

