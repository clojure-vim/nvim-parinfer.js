(ns nvim-parinfer.change-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [nvim-parinfer.core :as core]))

(defn- unpatch
  [result event]
  (if-not result
    event
    (assoc
      result
      "lines"
      (->> (get result "patch")
       (mapcat
         (fn [[first-n ls]]
           (map vector (range first-n (+ first-n (count ls))) ls)))
       (reduce
         (fn [ls [n l]]
           (assoc ls (dec n) l))
         (get event "lines"))))))

(defn- result-of
  [m]
  (-> m
    clj->js
    core/process
    js->clj
    (unpatch m)))

(deftest t-handles-indent-mode
  (is (= (-> (result-of
              {"position" [0 1 1 0]
               "lines" ["(a" "(b)"]
               "mode" "indent"})
           (get "lines"))
         ["(a)" "(b)"]))
  (testing "g:preview_cursor_scope"
    (let [result (result-of
                    {"position" [0 2 5 0]
                     "lines" ["(a [a" "   "]
                     "mode" "indent"
                     "preview_cursor_scope" 0})]
      (is (= (get result "lines") ["(a [a])" "   "])))
    (let [result (result-of
                  {"position" [0 2 5 0]
                   "lines" ["(a [a" "    "]
                   "mode" "indent"
                   "preview_cursor_scope" 1})]
     (is (= (get result "lines") ["(a [a" "    ])"]))
     (is (= (get result "position") [0 2 5 0])))))
         
(deftest t-handles-paren-mode
  (let [result (result-of
                {"position" [0 2 2 0]
                 "lines" ["(a" "b)"]
                 "mode" "paren"})]
    (is (= (get result "lines") ["(a" " b)"]))
    (is (= (get result "position") [0 2 3 0]))))

(deftest t-handles-failure
  (let [result (result-of
                 {"position" [0 1 4 0]
                  "lines" [")(}}]["]
                  "mode" "paren"})]
    (is (= (get result "lines") [")(}}]["]))
    (is (= (get result "position") [0 1 4 0]))))

(deftest t-enter-always-uses-paren-mode
  (let [result (result-of
                {"event" "BufEnter"
                 "position" [0 2 2 0]
                 "lines" ["(a" "b)"]
                 "mode" "indent"})]
    (is (= (get result "lines") ["(a" " b)"]))))
