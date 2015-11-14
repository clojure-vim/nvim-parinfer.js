(ns nvim-parinfer.main
  (:require [parinfer.indent-mode :as indent-mode]
            [clojure.string :as string]))

(defn dbg
  [msg & args]
  (if (exists? js/debug)
    (apply js/debug msg (map pr-str args))
    (apply js/console.log msg (map pr-str args)))
  (first args))

(defn split-lines [s]
  (string/split s #"\r?\n" -1))

(def buffer-results (atom {}))

(defn simple-diff [old-lines current-lines]
  {:line-no [0 (count old-lines)]
   :new-line current-lines})

(defn run-indent [old-lines current-lines opts current-result]
  (if current-result
    (indent-mode/format-text-change (string/join "\n" current-lines) (:state current-result) (simple-diff old-lines current-lines) opts)
    (indent-mode/format-text (string/join "\n" current-lines) opts)))

(defn format-lines [current-lines cursor-x cursor-line buffer-results bufnum]
  (try
   (let [opts {:cursor-x (dec cursor-x) :cursor-line (dec cursor-line)}
         current-result (get @buffer-results bufnum)
         old-text (:text current-result)
         old-lines (split-lines old-text)]

     ;; current-lines has changed from parinfer's state
     (when (not= old-lines current-lines)
       (let [new-result (run-indent old-lines current-lines opts current-result)
             new-text (:text new-result)
             new-lines (split-lines new-text)]

         (when-not (:valid? new-result)
           (dbg "invalid parinfer result" new-result))

         (swap! buffer-results assoc bufnum new-result)

         ;; parinfer changed input
         (when (not= new-lines current-lines)
           new-lines))))

   (catch js/Error e
     (dbg "EXCEPTION" e))))

(defn process-lines! [buf lines cursor-x cursor-line bufnum]
  (try
   (when-let [new-lines (format-lines (js->clj lines) cursor-x cursor-line buffer-results bufnum)]
     (dbg "parinfer changed something" (count new-lines))
     (.setLineSlice buf 0 -1 true true (clj->js new-lines)))

   (catch js/Error e
     (dbg "format-lines EXCEPTION" e))))

(defn format-buffer [nvim cursor]
  (try
   (let [[_ cursor-line cursor-x _] cursor
         start (js/Date.)]
     (.getCurrentBuffer nvim
                        (fn [err buf]
                          (when err (js/debug err))
                          (.getNumber buf (fn [err bufnum]
                                            (when err (js/debug err))
                                            (.getLineSlice buf 0 -1 true true
                                                           (fn [err lines]
                                                             (when err (js/debug err))
                                                             (process-lines! buf lines cursor-x cursor-line bufnum)
                                                             #_
                                                             (dbg "time" (- (.getTime (js/Date.)) (.getTime start))))))))))
   (catch js/Error e
     (dbg "format-buffer" e))))

(defn -main []
  (try
   (when (exists? js/plugin)
     (js/debug "hello nvim")
     (.autocmdSync js/plugin "BufEnter" #js {:pattern "*.cljs,*.clj,*.edn" :eval "getpos('.')"} format-buffer)
     (.autocmdSync js/plugin "TextChanged,TextChangedI" #js {:pattern "*.cljs,*.clj,*.edn" :eval "getpos('.')"} format-buffer))
   (catch js/Error e
     (dbg "main exception" e))))

(set! *main-cli-fn* -main)
