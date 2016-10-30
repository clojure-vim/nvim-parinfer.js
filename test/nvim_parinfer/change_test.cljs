(ns nvim-parinfer.change-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [nvim-parinfer.core :refer [text-changed]]))

(deftest t-handles-indent-mode
  (is (= (-> (text-changed
              {:cursor [0 0]
               :lines ["(a" "(b)"]
               :mode "indent"})
           :lines)
         ["(a)" "(b)"]))
  (testing ":preview-cursor-scope"
    (let [result (text-changed
                    {:cursor [1 4]
                     :lines ["(a [a" "   "]
                     :mode "indent"
                     :preview-cursor-scope false})]
      (is (= (:lines result) ["(a [a])" "   "])))
    (let [result (text-changed
                   {:cursor [1 4]
                    :lines ["(a [a" "    "]
                    :mode "indent"
                    :preview-cursor-scope true})]
      (is (= (:lines result) ["(a [a" "    ])"]))
      (is (= (:cursor result) [1 4])))))
         
(deftest t-handles-paren-mode
  (let [result (text-changed
                {:cursor [1 1]
                 :lines ["(a" "b)"]
                 :mode "paren"})]
    (is (= (:lines result) ["(a" " b)"]))
    (is (= (:cursor result) [1 2]))))

(deftest t-handles-failure
  (let [result (text-changed
                 {:cursor [0 3]
                  :lines [")(}}]["]
                  :mode "paren"})]
    (is (= (:lines result) [")(}}]["]))
    (is (= (:cursor result) [0 3]))))
