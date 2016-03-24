(ns nvim-parinfer.main
  (:require
   [clojure.string :as string]
   [parinfer :as parinfer]))

(defn dbg
  [msg & args]
  (if (exists? js/debug)
    (apply js/debug msg (map pr-str args))
    (apply js/console.log msg (map pr-str args)))
  (first args))

(defn split-lines [s]
  (when s
    (.split s #"\r?\n")))

(defn new-line? [current-line]
  (re-matches #"\s*\S$" current-line))

(defn run-indent [current-text opts mode current-line]
  (cond
   (= "indent" mode)
   (parinfer/indentMode current-text opts)

   (= "paren" mode)
   (parinfer/parenMode current-text opts)

   (= "hybrid" mode)
   (if (new-line? current-line)
     (parinfer/indentMode current-text opts)
     (let [paren (parinfer/parenMode current-text opts)]
       (parinfer/indentMode (.-text paren) opts)))))

(defn format-lines [current-lines cursor-x cursor-line bufnum mode]
  (try
   (let [opts #js {"cursorX" (dec cursor-x) "cursorLine" (dec cursor-line)}
         current-text (.join current-lines "\n")
         result (run-indent current-text opts mode (get current-lines (dec cursor-line)))
         new-text (.-text result)]
    (when (not= current-text new-text)
     (split-lines new-text)))

   (catch :default e
     (dbg "EXCEPTION" e e.stack))))

(defn parinfer-indent
  [nvim args [[_ cursor-line cursor-x _] bufnum lines mode] nvim-callback]
  (let [start (js/Date.)]
    (if-let [new-lines (format-lines lines cursor-x cursor-line bufnum mode)]
      (do
       #_(js/debug "c" (- (.getTime (js/Date.)) (.getTime start)))
       (nvim-callback nil new-lines))
      (do
       #_(js/debug "n" (- (.getTime (js/Date.)) (.getTime start)))
       (nvim-callback nil #js [])))))

(defn -main []
  (try
   (when (exists? js/plugin)
     (js/debug "hello parinfer")
     (.functionSync js/plugin "ParinferIndent"
                    #js {:eval "[getpos('.'), bufnr('.'), getline(1,line('$')), g:parinfer_mode]"}
                    parinfer-indent))
   (catch :default e
     (dbg "main exception" e e.stack))))

(set! *main-cli-fn* -main)
