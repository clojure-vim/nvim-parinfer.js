(ns nvim-parinfer.core
  (:require [clojure.string :as string]
            [parinfer :as parinfer]))

(defn- reindent
  [mode text options]
  (if (= mode "indent")
    (parinfer/indentMode text options)
    (parinfer/parenMode text options)))

(defn text-changed
  [{:keys [:parinfer/lines :parinfer/mode]
    [cursorLine cursorX] :parinfer/cursor
    :as event}]
  (let [result (reindent
                 mode
                 (string/join "\n" lines)
                 (clj->js {"cursorX" cursorX
                           "cursorLine" cursorLine}))]
    (assoc event :parinfer/lines (string/split (aget result "text") #"\n"))))
