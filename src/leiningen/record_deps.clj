(ns leiningen.record-deps
  (:require [clojure.java.shell :refer [sh]]
            [leiningen.deps :as ld]
            [leiningen.core.main :as lm]
            [leiningen.core.project :as lp]
            [leiningen.core.classpath :as lcp]
            [clojure.pprint :as pp]
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

(defn save-deps [project]
  (let [{:keys [record-deps-edn record-deps-txt]} project
        hierarchy (lcp/managed-dependency-hierarchy :dependencies
                                                    :managed-dependencies
                                                    project)]
    (when record-deps-txt
      (lm/info "Saving project dependencies in" record-deps-txt "as text.")
      (with-open [out (io/writer record-deps-txt)]
        (binding [*out* out]
          (walk-deps hierarchy print-dep))))
    (when record-deps-edn
      (lm/info "Saving project dependencies in" record-deps-edn "as EDN data.")
      (with-open [out (io/writer record-deps-edn)]
        (pp/pprint hierarchy out)))))

(defn record-deps
  "Write the dependency tree to a file.  Where and what type of file is
  saved depends on the project map keys :record-deps-edn
  and :record-deps-txt.  The former specifies the pathname of the EDN
  data and the latter the pathname of the txt description.  The
  dependencies are those you would normally get with the command
  \"lein deps :tree-data\" (for the former) or \"lein
  deps :tree\" (for the latter)."
  [project & args]
  (save-deps project))
