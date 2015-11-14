(ns ^:figwheel-always nvim-parinfer.main-test
    (:require [cljs.nodejs :as nodejs]
              [cljs.test :refer-macros [deftest is testing run-tests]]
              [nvim-parinfer.main :as m]))

(nodejs/enable-util-print!)
(println "Hello from the Node!")
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(deftest testing
  (is (= nil (m/format-lines ["a" "b" ""] 0 0 (atom {}) 0)))
  (is (= ["(a)" "b" ""] (m/format-lines ["(a" "b" ""] 0 0 (atom {}) 0)))
  (is (= [""] (m/format-lines [] 0 0 (atom {}) 0)))
  (let [buf-results (atom {})]
    (is (= ["(a" " b)" "c"] (m/format-lines ["(a" " b" "c"] 0 0 buf-results 0)))
    (is (= ["(a)" "c"] (m/format-lines ["(a" "c"] 0 0 buf-results 0)))))

(defn test-it []
  (run-tests))
