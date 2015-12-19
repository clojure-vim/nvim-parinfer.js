(ns nvim-parinfer.main
  (:require
   [parinfer.indent-mode :as indent-mode]
   [parinfer.paren-mode :as paren-mode]
   [clojure.data :as cd]
   [clojure.string :as string]
   [cljsjs.jsdiff]))

(defn dbg
  [msg & args]
  (if (exists? js/debug)
    (apply js/debug msg (map pr-str args))
    (apply js/console.log msg (map pr-str args)))
  (first args))

(defn clean-value [value]
  (string/replace value #"\r?\n$" ""))

(defn split-lines [s]
  (when s
    (string/split (clean-value s) #"\r?\n" -1)))

(def buffer-results (atom {}))

(defn index-jsdiff [diff]
  (-> (reduce (fn [changes [i d]]
               (let [{:strs [added removed value] :as c} (js->clj d)
                     numchange (get c "count" 1)
                     skipped (:skipped changes)
                     patched (:patched changes)]
                 (cond-> changes
                   (and (or added removed) patched)
                   (->
                     (update :new-line concat (split-lines (:skipped-value changes)))
                     (dissoc :skipped-value))

                   added
                   (->
                     (update :new-line concat (split-lines value)))

                   (and (or added removed) (not patched))
                   (->
                      (update-in [:line-no 0] + skipped)
                      (assoc :patched true)
                      (dissoc :skipped-value))

                   removed
                   (->
                     (update-in [:line-no 1] + skipped numchange)
                     (dissoc :skipped))

                   (not (or added removed))
                   (->
                     (assoc :skipped-value value)
                     (update :skipped + numchange)))))
             {:line-no [0 0]
              :new-line []}
             (map-indexed vector diff))
      (dissoc :skipped :skipped-value :patched)
      (update :line-no (fn [[a b]] [a (max a b)]))))

(def jsdiff
  (if (exists? js/JsDiff)
    js/JsDiff
    js/module.exports))

(defn text-diff [old-text current-text]
  (let [diff (jsdiff.diffLines old-text current-text)]
    (index-jsdiff diff)))

(defn data-diff [old-lines current-lines]
  (text-diff
   (string/join "\n" old-lines)
   (string/join "\n" current-lines)))

(defn run-indent [old-state old-text current-text opts mode]
  (cond
   (= "indent" mode)
   (if old-text
     (indent-mode/format-text-change current-text old-state (text-diff old-text current-text) opts)
     ;; Run paren-mode format to fix any bad indentation first - otherwise this can really mess up badly formatted files
     (indent-mode/format-text (:text (paren-mode/format-text current-text opts)) opts))

   (= "paren" mode)
   (paren-mode/format-text current-text opts)))

(defn format-lines [current-lines cursor-x cursor-line buffer-results bufnum mode]
  (try
   (let [opts {:cursor-x (dec cursor-x) :cursor-line (dec cursor-line)}
         current-text (clean-value (string/join "\n" current-lines))
         old-result (get @buffer-results bufnum)
         old-text (:text old-result)]

     ;; current-lines has changed from parinfer's state
     (when (not= old-text current-text)
       (let [new-result (run-indent (:state old-result) old-text current-text opts mode)
             new-text (:text new-result)]

         (when (:valid? new-result)
           (if (= "indent" mode)
             (swap! buffer-results assoc bufnum new-result)
             (swap! buffer-results assoc bufnum nil))

           ;; parinfer changed input
           (when (not= new-text current-text)
             (split-lines new-text))))))

   (catch :default e
     (dbg "EXCEPTION" e e.stack))))

(defn parinfer-indent
  [nvim args [[_ cursor-line cursor-x _] bufnum lines mode]]
  (let [start (js/Date.)]
    (if-let [new-lines (format-lines (js->clj lines) cursor-x cursor-line buffer-results bufnum mode)]
      (do
       #_(js/debug "c" (- (.getTime (js/Date.)) (.getTime start)))
       (clj->js new-lines))
      (do
       #_(js/debug "n" (- (.getTime (js/Date.)) (.getTime start)))
       (clj->js [])))))

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
