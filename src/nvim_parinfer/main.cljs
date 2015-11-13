(ns nvim-parinfer.main
  (:require [parinfer.indent-mode :as indent-mode]
            [clojure.string :as string]))

(defn dbg
  ([v]
   (dbg "DEBUG" v))
  ([msg v]
   (if (exists? js/debug)
     (js/debug msg (pr-str v))
     (js/console.log msg (pr-str v)))
   v))

(def buffer-results (atom {}))

(defn diff [old-text new-text]
  (try
   (let [differ (if (exists? js/JsDiff) js/JsDiff (js/require "diff"))
         diff (.structuredPatch differ "old" "old" old-text new-text "old" "new" #js {"context" 0})
         hunks (aget diff "hunks")
         hunk (first hunks)
         start (dec (aget hunk "oldStart"))
         num-lines (aget hunk "oldLines")
         lines (js->clj (aget hunk "lines"))
         change {:line-no [start (+ start num-lines)]
                 :new-line (map (fn [line] (subs line 1)) (filter (fn [line] (= \+ (first line))) lines))}]

     ;; TODO deal with multiple hunks?
     (when (> (count hunks) 1)
       (js/debug "EXTRA HUNKS" hunks))

     (comment
      (dbg jdiff)
      (dbg diff)
      (dbg (= jdiff diff))
      (js/debug diff)
      (js/debug hunk)
      (js/debug start)
      (js/debug num-lines)
      (js/debug lines)
      (dbg "change" change))
     change)
   (catch js/Error e
     (dbg "DIFF EXCEPTION" e))))

(defn format-text [current-lines [_ cursor-line cursor-x _] buffer-results bufnum]
  (try
   (let [current-text (string/join "\n" current-lines)
         opts {:cursor-x (dec cursor-x) :cursor-line (dec cursor-line)}
         res (get @buffer-results bufnum)
         old-text (:text res)]

     ;; current-text has changed from parinfer's state
     (when (not= old-text current-text)
       (let [new-result (if res
                          (indent-mode/format-text-change current-text (:state res) (diff old-text current-text) opts)
                          (indent-mode/format-text current-text opts))
             new-text (:text new-result)]

         (when-not (:valid? new-result)
           (dbg "invalid parinfer result" new-result))

         (swap! buffer-results assoc bufnum new-result)

         ;; parinfer changed input
         (when (not= new-text current-text)
           new-text))))

   (catch js/Error e
     (dbg "EXCEPTION" e))))

(defn format-lines [buf lines cursor bufnum]
  (try
   (when-let [new-text (format-text lines cursor buffer-results bufnum)]
     (let [new-lines (string/split-lines new-text)]
      (dbg "parinfer changed something" (count new-lines))
      (.setLineSlice buf 0 -1 true true (clj->js new-lines))))

   (catch js/Error e
     (dbg "format-lines EXCEPTION" e))))

(defn format-buffer [nvim cursor]
  (try
   (.getCurrentBuffer nvim
                      (fn [err buf]
                        (.getNumber buf (fn [err bufnum]
                                          (.getLineSlice buf 0 -1 true true
                                                         (fn [err lines]
                                                           (format-lines buf lines cursor bufnum)))))))
   (catch js/Error e
     (dbg "format-buffer" e))))

(defn -main []
  (when (exists? js/plugin)
    (js/debug "hello nvim")
    (.autocmdSync js/plugin "BufEnter" #js {:pattern "*.cljs,*.clj,*.edn" :eval "getpos('.')"} format-buffer)
    (.autocmdSync js/plugin "TextChanged,TextChangedI" #js {:pattern "*.cljs,*.clj,*.edn" :eval "getpos('.')"} format-buffer)))

(set! *main-cli-fn* -main)
