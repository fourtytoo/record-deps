# record-deps

A Leiningen plugin that lets you automatically include the dependency
tree of your project into the final (uber)jar.

On every build (either jar or uberjar), this plugin writes the
dependency tree to a file in the directory `resources`.  That implies
that the list of dependencies will be included in the jar file as a
Java resource.

Later you can check the dependencies that formed the final jar as with
`lein deps :tree` but without the project.  You can, for instance,
extract the dependencies like this

```console
 $ jar xf path-to-your.jar deps.txt
```

or something along the lines.  See below.


## Install

Clone and install locally:

```console
 $ git clone https://github.com/fourtytoo/record-deps.git
 $ lein install
```


## Usage

Put `[record-deps "0.1.1-SNAPSHOT"]` into the `:plugins` vector of
your project.clj.  You can also add something like `:record-deps-txt
"resources/project_dependencies"` to your project map, if you don't
like the default.

Check that it works

```console
 $ lein record-deps
 $ cat resources/deps.txt
```

Where and what type of file is saved depends on the project map keys
`:record-deps-edn` and `:record-deps-txt`.  The former specifies the
pathname of the EDN data and the latter the pathname of the textual
description.  Whichever you are more comfortable with.  If you don't
specify anything at all the default is to write a text file
`resources/deps.txt`.

Add `record-deps` to your `:prep-tasks` like this:

```clojure
:prep-tasks ["record-deps" "javac" "compile"]
```

This creates the dependencies file before performing the compilation.

Test it

```console
 $ lein uberjar
 Saving project dependencies in resources/deps.txt as text.
 Created /usr/home/your_account/some/project.dir/target/your_project-0.1.1-SNAPSHOT.jar
 $ jar tf /usr/home/your_account/some/project.dir/target/your_project-0.1.0-SNAPSHOT.jar | fgrep deps.txt
 deps.txt
 $
```


## Query

If you want to avoid extracting the file from the jar every time you
want to check the dependencies of your jar, you may want to include
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

```console
 $ java -cp target/my-program-standalone.jar resource deps.edn
```

## License

Copyright © 2018 Walter C. Pelissero <walter@pelissero.de>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
