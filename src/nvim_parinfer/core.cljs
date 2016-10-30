(ns nvim-parinfer.core
  (:require [clojure.string :as string]
            [parinfer :as parinfer]))

(defn text-changed
  [{:keys [:parinfer/lines]
    [cursorLine cursorX] :parinfer/cursor
    :as event}]
  (let [result (parinfer/indentMode
                 (string/join "\n" lines)
                 (clj->js {"cursorX" cursorX
                           "cursorLine" cursorLine}))]
    (assoc event :parinfer/lines (string/split (aget result "text") #"\n"))))
