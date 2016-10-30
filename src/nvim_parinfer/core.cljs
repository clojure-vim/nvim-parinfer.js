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
  [{[cursorLine cursorX] :cursor
    :as event}]
  (let [lines (get event "lines")
        previewCursorScope? (some-> event
                              (get "parinfer_preview_cursor_scope")
                              pos?)
        result (reindent
                 (get event "parinfer_mode")
                 (string/join "\n" lines)
                 {:cursorX cursorX
                  :cursorLine cursorLine
                  :previewCursorScope previewCursorScope?})]
    (-> event
      (assoc "lines" (string/split (:text result) #"\n"))
      (assoc-in [:cursor 1] (:cursorX result)))))
