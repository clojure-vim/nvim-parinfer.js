(ns ^:figwheel-always nvim-parinfer.test-runner
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :refer-macros [run-tests]]
            [nvim-parinfer.change-test]))

(nodejs/enable-util-print!)

(def -main (fn [] nil))
(set! *main-cli-fn* -main)

(defn test-it []
  (run-tests 'nvim-parinfer.change-test))
