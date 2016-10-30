(ns nvim-parinfer.change-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [nvim-parinfer.core :as core]))

(defn- result-of
  [m]
  (-> m
    clj->js
    core/text-changed
    js->clj))

(deftest t-handles-indent-mode
  (is (= (-> (result-of
              {"position" [0 0]
               "lines" ["(a" "(b)"]
               "parinfer_mode" "indent"})
           (get "lines"))
         ["(a)" "(b)"]))
  (testing "g:parinfer_preview_cursor_scope"
    (let [result (result-of
                    {"position" [1 4]
                     "lines" ["(a [a" "   "]
                     "parinfer_mode" "indent"
                     "parinfer_preview_cursor_scope" 0})]
      (is (= (get result "lines") ["(a [a])" "   "])))
    (let [result (result-of
                   {"position" [1 4]
                    "lines" ["(a [a" "    "]
                    "parinfer_mode" "indent"
                    "parinfer_preview_cursor_scope" 1})]
      (is (= (get result "lines") ["(a [a" "    ])"]))
      (is (= (get result "position") [1 4])))))
         
(deftest t-handles-paren-mode
  (let [result (result-of
                {"position" [1 1]
                 "lines" ["(a" "b)"]
                 "parinfer_mode" "paren"})]
    (is (= (get result "lines") ["(a" " b)"]))
    (is (= (get result "position") [1 2]))))

(deftest t-handles-failure
  (let [result (result-of
                 {"position" [0 3]
                  "lines" [")(}}]["]
                  "parinfer_mode" "paren"})]
    (is (= (get result "lines") [")(}}]["]))
    (is (= (get result "position") [0 3]))))
