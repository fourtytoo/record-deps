(ns leiningen.record-deps
  (:require [clojure.java.shell :refer [sh]]
            [leiningen.deps :as ld]
            [leiningen.core.main :as lm]
            [leiningen.core.project :as lp]
            [leiningen.core.classpath :as lcp]
            [clojure.java.io :as io]
            [clojure.string :as s]))

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

(defn record-deps
  "Write the dependency tree to a file.  Where and what type of file is
  saved depends on the keys :edn and :txt.  The former specifies the
  pathname of the EDN data and the latter the pathname of the txt
  description.  The dependencies are those you would normally get with
  the command \"lein deps :tree-data\" (for the former) or \"lein
  deps :tree\" (for the latter)."
  [project & {:keys [edn txt]}]
  (let [hierarchy (lcp/managed-dependency-hierarchy :dependencies
                                                    :managed-dependencies
                                                    project)
        ;; by default write at least the text file
        txt (if (or txt edn)
              txt
              (io/file "resources" "deps.txt"))]
    (when txt
      (lm/info "Saving project dependencies in" (str txt) "as text.")
      (with-out txt
        (walk-deps hierarchy print-dep)))
    (when edn
      (lm/info "Saving project dependencies in" (str edn) "as EDN data.")
      (with-out edn
        (prn hierarchy)))))
