(ns leiningen.record-deps
  (:require [clojure.java.shell :refer [sh]]
            [leiningen.deps :as ld]
            [leiningen.core.main :as lm]
            [leiningen.core.project :as lp]
            [leiningen.core.classpath :as lcp]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.edn :as edn]
            [clojure.data :refer [diff]]))

(defn- print-dep [dep level]
  (println (str (s/join (repeat level "   "))
                (pr-str dep))))

(defn- walk-deps
  ([deps f level]
   (doseq [[dep subdeps] deps]
     (f dep level)
     (walk-deps subdeps f (inc level))))
  ([deps f]
   (walk-deps deps f 0)))

(defmacro with-out [file & body]
  `(with-open [out# (io/writer ~file)]
     (binding [*out* out#]
       ~@body)))

(defn file-exists? [file]
  (.exists (io/as-file file)))

(defn file? [thing]
  (instance? java.io.File thing))

(defn flatten-dependencies [deps]
  (letfn [(fl [m deps]
            (reduce-kv (fn [m [lib version & _] deps]
                         (if deps
                           (fl (assoc m lib version) deps)
                           (assoc m lib version)))
                       m deps))]
    (fl {} deps)))

(defn common-dependencies [d1 d2]
  [(select-keys d1 (keys d2))
   (select-keys d2 (keys d1))])

(defn compare-dependencies [d1 d2]
  (->> (diff (flatten-dependencies d1)
             (flatten-dependencies d2))
       (take 2)
       (apply common-dependencies)))

(defn record-deps
  "Write the dependency tree to a file.  Where and what type of file is
  saved depends on the keys :edn and :txt.  The former specifies the
  pathname of the EDN data and the latter the pathname of the txt
  description.  The dependencies are those you would normally get with
  the command \"lein deps :tree-data\" (for the former) or \"lein
  deps :tree\" (for the latter)."
  [project & {:keys [edn txt check] :as options}]
  (let [hierarchy (lcp/managed-dependency-hierarchy :dependencies
                                                    :managed-dependencies
                                                    project)
        edn (cond (or (file? check)
                      (string? check)) check
                  (true? check) (or edn
                                    (io/file "resources" "deps.edn"))
                  :else edn)
        ;; by default write at least the text file
        txt (if (or txt edn)
              txt
              (io/file "resources" "deps.txt"))]
    (when (and check
               (file-exists? edn))
      (lm/info "Checking dependency version...")
      (let [old (edn/read-string (slurp (io/file (:root project) edn)))
            [os ns] (compare-dependencies old hierarchy)]
        (if (or os ns)
          (do
            (lm/warn "Dependency version change detected; if this is intended delete"
                     (str edn))
            (run! lm/info os)
            (run! lm/info ns)
            (System/exit -1))
          (lm/info "No dependency version change."))))
    (when txt
      (lm/info "Saving project dependencies in" (str txt) "as text.")
      (with-out (io/file (:root project) txt)
        (walk-deps hierarchy print-dep)))
    (when (and edn
               (or (not check)
                   (not (file-exists? edn))))
      (lm/info "Saving project dependencies in" (str edn) "as EDN data.")
      (with-out (io/file (:root project) edn)
        (prn hierarchy)))))
