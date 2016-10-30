(ns nvim-parinfer.core
  (:require [clojure.string :as string]
            [parinfer :as parinfer]))

(def ^:private parinfer-mode-fn
  {"indent" parinfer/indentMode
   "paren" parinfer/parenMode})

(defn- reindent
  "Wrapper for *Mode, translating to/from JS structures."
  [mode text options]
  (let [result ((parinfer-mode-fn mode) text (clj->js options))]
    {:success (aget result "success")
     :text (aget result "text")
     :cursorX (aget result "cursorX")}))

(defn text-changed
  [{:keys [lines mode preview-cursor-scope]
    [cursorLine cursorX] :cursor
    :as event}]
  (let [result (reindent
                 mode
                 (string/join "\n" lines)
                 {:cursorX cursorX
                  :cursorLine cursorLine
                  :previewCursorScope preview-cursor-scope})]
    (-> event
      (assoc :lines (string/split (:text result) #"\n"))
      (assoc-in [:cursor 1] (:cursorX result)))))
