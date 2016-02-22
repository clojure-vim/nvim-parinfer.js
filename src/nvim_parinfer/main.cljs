(ns nvim-parinfer.main
  (:require
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

(defn run-indent [current-text opts mode]
  (cond
   (= "indent" mode)
   (parinfer/indentMode current-text opts)

   (= "paren" mode)
   (parinfer/parenMode current-text opts)))

(defn format-lines [current-lines cursor-x cursor-line bufnum mode]
  (try
   (let [opts #js {"cursorX" (dec cursor-x) "cursorLine" (dec cursor-line)}
         current-text (.join current-lines "\n")
         result (run-indent current-text opts mode)
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
       (nvim-callback nil (clj->js new-lines)))
      (do
       #_(js/debug "n" (- (.getTime (js/Date.)) (.getTime start)))
       (nvim-callback nil (clj->js []))))))

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
