(ns ^:figwheel-always nvim-parinfer.main-test
    (:require [cljs.nodejs :as nodejs]
              [cljs.test :refer-macros [deftest is testing run-tests]]
              [clojure.string :as string]
              [nvim-parinfer.main :as m]))

(nodejs/enable-util-print!)
(println "Hello from the Node!")
(def -main (fn [] nil))
(set! *main-cli-fn* -main) ;; this is required

(deftest format-lines
  (is (= nil (m/format-lines ["a" "b" ""] 0 0 (atom {}) 0)))
  (is (= ["(a)" "b"] (m/format-lines ["(a" "b"] 0 0 (atom {}) 0)))
  (is (= nil (m/format-lines [] 0 0 (atom {}) 0)))
  (let [buf-results (atom {})]
    (is (= ["(a" " b)" "c"] (m/format-lines ["(a" " b" "c"] 0 0 buf-results 0)))
    (is (= ["(a)" "c"] (m/format-lines ["(a" "c"] 0 0 buf-results 0)))
    (is (= nil (m/format-lines ["(a)"] 0 0 buf-results 0))))
  (testing "corrects-bad-indentation-initially"
    (let [buf-results (atom {})]
      (is (= ["(a" " b)"] (m/format-lines ["(a" "b)"] 0 0 buf-results 0)))
      (is (= ["(a)" "b"] (m/format-lines ["(a" "b)"] 0 0 buf-results 0))))))

(deftest diffing
  (testing "no change"
    (is (= {:line-no [0 0] :new-line []}
           (m/data-diff ["a"] ["a"])))
    (is (= {:line-no [0 0] :new-line []}
           (m/data-diff ["a" "b"] ["a" "b"]))))
  (testing "change all"
    (is (= {:line-no [0 1] :new-line ["1"]}
           (m/data-diff ["a"] ["1"])))
    (is (= {:line-no [0 2] :new-line ["1" "2"]}
           (m/data-diff ["a" "b"] ["1" "2"]))))
  (testing "add at end"
    (is (= {:line-no [1 2] :new-line ["b" "3"]}
           (m/data-diff ["a" "b"] ["a" "b" "3"])))
    (is (= {:line-no [1 2] :new-line ["2" "3"]}
           (m/data-diff ["a" "b"] ["a" "2" "3"])))
    (is (= {:line-no [1 2] :new-line ["2" "3" "4"]}
           (m/data-diff ["a" "b"] ["a" "2" "3" "4"])))
    (is (= {:line-no [1 2] :new-line ["2" "3" "4"]}
           (m/data-diff ["a" "b"] ["a" "2" "3" "4"])))
    (is (= {:line-no [2 3] :new-line ["3" "4"]}
           (m/data-diff ["a" "b" "3"] ["a" "b" "3" "4"])))
    (is (= {:line-no [2 3] :new-line ["3"]}
           (m/data-diff ["a" "b" "3"] ["a" "b" "3" ""]))))
  (testing "add to start"
    (is (= {:line-no [0 0] :new-line ["1"]}
           (m/data-diff ["a" "b" "c"] ["1" "a" "b" "c"]))))
  (testing "add to start and end"
    (is (= {:line-no [0 3] :new-line ["1" "a" "b" "c" "2"]}
           (m/data-diff ["a" "b" "c"] ["1" "a" "b" "c" "2"]))))
  (testing "remove from end"
    (is (= {:line-no [0 1] :new-line []}
           (m/data-diff ["a"] [])))
    (is (= {:line-no [0 2] :new-line ["a"]}
           (m/data-diff ["a" "b"] ["a"])))
    (is (= {:line-no [1 3] :new-line ["b"]}
           (m/data-diff ["a" "b" "c"] ["a" "b"]))))
  (testing "remove from start"
    (is (= {:line-no [0 1] :new-line []}
           (m/data-diff ["a" "b"] ["b"])))
    (is (= {:line-no [0 1] :new-line []}
           (m/data-diff ["a" "b" "c"] ["b" "c"]))))
  (testing "remove and change"
    (is (= {:line-no [1 3] :new-line ["x"]}
           (m/data-diff ["a" "b" "c"] ["a" "x"])))
    (is (= {:line-no [0 3] :new-line ["x" "b"]}
           (m/data-diff ["a" "b" "c"] ["x" "b"]))))
  (testing "change at start"
    (is (= {:line-no [0 1] :new-line ["1"]}
           (m/data-diff ["a" "b"] ["1" "b"])))
    (is (= {:line-no [0 2] :new-line ["1" "2"]}
           (m/data-diff ["a" "b" "c"] ["1" "2" "c"])))
    (is (= {:line-no [0 1] :new-line ["1"]}
           (m/data-diff ["a" "b" "c"] ["1" "b" "c"]))))
  (testing "change at end"
    (is (= {:line-no [1 2] :new-line ["2"]}
           (m/data-diff ["a" "b"] ["a" "2"]))))
  (testing "change in middle"
    (is (= {:line-no [1 2] :new-line ["x"]}
           (m/data-diff ["a" "b" "c"] ["a" "x" "c"])))
    (is (= {:line-no [1 2] :new-line [""]}
           (m/data-diff ["a" " " "c"] ["a" "" "c"])))
    (is (= {:line-no [1 3] :new-line ["1" "2"]}
           (m/data-diff ["a" "b" "c" "d"] ["a" "1" "2" "d"]))))
  (is (= {:line-no [1 15] :new-line ["c" "d" "1" "2" "e" "3" "g" "h" "i" "j" "l" "m"]}
         (m/data-diff ["a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o"]
                      ["a" "c" "d" "1" "2" "e" "3" "g" "h" "i" "j" "l" "m"]))))

(defn test-it []
  (run-tests))
