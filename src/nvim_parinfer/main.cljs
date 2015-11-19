(ns nvim-parinfer.main
  (:require [parinfer.indent-mode :as indent-mode]
            [parinfer.paren-mode :as paren-mode]
            [clojure.data :as cd]
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

(defn index-changes [[only-old only-cur in-both]]
  (reduce (fn [accum i]
            ;; default to false so I can use nil? to differentiate
            (let [old (get only-old i false)
                  cur (get only-cur i false)
                  both (get in-both i false)]
              (cond-> accum
                (and old (not (:old-start accum)))
                (assoc :old-start i)

                old
                (assoc :old-end i)

                (and cur (not (:new-start accum)))
                (assoc :new-start i)

                cur
                (assoc :new-end i)

                (and (nil? old) (not cur) both)
                (assoc :new-end i))))

          {}
          (range (max (count only-old) (count only-cur) (count in-both)))))

(defn data-diff [old-lines current-lines]
  (let [diff (cd/diff old-lines current-lines)
        [only-old only-cur both] diff]

    (cond
     (and only-old only-cur both)
     (let [diff (cd/diff old-lines current-lines)
           {:keys [old-start old-end new-start new-end]} (index-changes diff)
           new-line (subvec current-lines new-start (inc new-end))]
       {:line-no [old-start (inc old-end)]
        :new-line new-line})

     (and only-cur both)
     (let [{:keys [old-start old-end new-start new-end]} (index-changes diff)]
       {:line-no [new-start (inc new-start)]
        :new-line (subvec current-lines new-start (inc new-end))})

     (and only-old only-cur)
     {:line-no [0 (count only-old)] :new-line only-cur}

     (and only-old both)
     (let [e (count only-old)
           s (count both)]
       {:line-no [s e] :new-line []})

     both
     {:line-no [0 0] :new-line []})))

(defn run-indent [old-lines current-lines opts current-result]
  (if current-result
    (let [old-text (:text current-result)
          current-text (string/join "\n" current-lines)]
      (indent-mode/format-text-change current-text (:state current-result) (data-diff old-lines current-lines) opts))

    ;; Run paren-mode format to fix any bad indentation first - otherwise this can really mess up badly formatted files
    (indent-mode/format-text (:text (paren-mode/format-text (string/join "\n" current-lines))) opts)))

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

     #_
     (dbg "start")
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
                                                             (dbg "Done!" (- (.getTime (js/Date.)) (.getTime start)))))))))

     #_
     (dbg "time" (- (.getTime (js/Date.)) (.getTime start))))
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
