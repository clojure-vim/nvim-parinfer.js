(ns nvim-parinfer.core
  (:require [clojure.string :as string]
            [parinfer :as parinfer]))

(def ^:private parinfer-mode-fn
  {"indent" parinfer/indentMode
   "paren" parinfer/parenMode})

(defn- reindent
  "Wrapper for parinfer/*Mode, translating to/from JS structures."
  [mode text options]
  (let [options (clj->js options)
        result ((parinfer-mode-fn mode) text options)]
    {:success (aget result "success")
     :text (aget result "text")
     :cursorX (aget result "cursorX")}))

(defn text-changed
  [{:keys [:parinfer/lines :parinfer/mode]
    [cursorLine cursorX] :parinfer/cursor
    :as event}]
  (let [result (reindent
                 mode
                 (string/join "\n" lines)
                 {:cursorX cursorX
                  :cursorLine cursorLine})]
    (assoc event :parinfer/lines (string/split (:text result) #"\n"))))
