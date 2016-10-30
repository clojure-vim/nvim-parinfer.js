(ns nvim-parinfer.change-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [nvim-parinfer.core :refer [text-changed]]))

(deftest t-handles-indent-mode
  (is (= (-> (text-changed
              {:cursor [0 0]
               "lines" ["(a" "(b)"]
               "parinfer_mode" "indent"})
           (get "lines"))
         ["(a)" "(b)"]))
  (testing "g:parinfer_preview_cursor_scope"
    (let [result (text-changed
                    {:cursor [1 4]
                     "lines" ["(a [a" "   "]
                     "parinfer_mode" "indent"
                     "parinfer_preview_cursor_scope" 0})]
      (is (= (get result "lines") ["(a [a])" "   "])))
    (let [result (text-changed
                   {:cursor [1 4]
                    "lines" ["(a [a" "    "]
                    "parinfer_mode" "indent"
                    "parinfer_preview_cursor_scope" 1})]
      (is (= (get result "lines") ["(a [a" "    ])"]))
      (is (= (:cursor result) [1 4])))))
         
(deftest t-handles-paren-mode
  (let [result (text-changed
                {:cursor [1 1]
                 "lines" ["(a" "b)"]
                 "parinfer_mode" "paren"})]
    (is (= (get result "lines") ["(a" " b)"]))
    (is (= (:cursor result) [1 2]))))

(deftest t-handles-failure
  (let [result (text-changed
                 {:cursor [0 3]
                  "lines" [")(}}]["]
                  "parinfer_mode" "paren"})]
    (is (= (get result "lines") [")(}}]["]))
    (is (= (:cursor result) [0 3]))))
