# record-deps

A Leiningen plugin to write the project dependency tree to a file.

The intention is that the output file will be automatically included
in the final (uber)jar as a resource.  Later you can check the
dependency tree of the jar file as with `lein deps :tree` but without
the project source tree.  You will have to extract the dependencies
like this

   $ jar xf path-to-your.jar deps.txt

or something along the lines.  See below.


## Install

   $ git clone https://github.com/fourtytoo/record-deps.git
   $ lein install

## Usage

Put `[record-deps "0.1.0-SNAPSHOT"]` into the `:plugins` vector of
your project.clj.  Add also something like `:record-deps-txt
"resources/deps.txt"` to your project map.

Check that it works

```console
    $ lein record-deps
    $ cat resources/deps.txt
```

Where and what type of file is saved depends on the project map keys
`:record-deps-edn` and `:record-deps-txt`.  The former specifies the
pathname of the EDN data and the latter the pathname of the textual
description.  Whichever you are more comfortable with.


## Query

If you want to avoit extracting the file from the jar every time you
want to check the dependency tree of a jar, you may want to include
this code in your project:

```clojure
(ns resource
  (:require [clojure.java.io :as io])
  (:gen-class :main))

(defn -main [& args]
  (doseq [r args]
    (let [r' (io/resource r)]
      (if r'
        (do (println (str "\n>> " r ":"))
            (println (slurp r')))
        (println "Missing" r)))))
```

and then from the command line you can simply

   $ java -cp target/my-program-standalone.jar resource deps.edn

## License

Copyright © 2018 Walter C. Pelissero <walter@pelissero.de>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
