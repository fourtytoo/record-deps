# record-deps

[![Clojars Project](https://img.shields.io/clojars/v/fourtytoo/record-deps.svg)](https://clojars.org/fourtytoo/record-deps)

A Leiningen plugin that lets you automatically include the dependency
tree of your project into the final (uber)jar.

On every build (either jar or uberjar), this plugin writes the
dependency tree to a file in the directory `resources`.  That implies
that the list of dependencies will be included in the jar file as a
Java resource.

Later you can check the dependencies that formed the final jar as with
`lein deps :tree` but without the project.  You can, for instance,
look at the dependencies like this

```console
 $ jar xf path-to-your.jar deps.txt
 $ cat deps.txt
```

See below for an alternative.


## Install

Clone and install locally:

```console
 $ git clone https://github.com/fourtytoo/record-deps.git
 $ lein install
```


## Usage

Put `[record-deps "0.3.2-SNAPSHOT"]` into the `:plugins` vector of
your project.clj.

Check that it works

```console
 $ lein record-deps
 $ cat resources/deps.txt
```

Add `record-deps` to your `:prep-tasks` like this:

```clojure
:prep-tasks ["record-deps" "javac" "compile"]
```

This creates the dependencies file before performing the compilation.

Where and what type of file is saved depends on the optional keys
`:edn` and `:txt`.  The former specifies the pathname of the EDN data
and the latter the pathname of the textual description.  Whichever you
are more comfortable with.  If you don't specify anything, the default
is to write the text file `resources/deps.txt`.

To instead create an EDN file you could write

```clojure
:prep-tasks [["record-deps" :edn "resources/dependencies.edn"] "javac" "compile"]
```

Test it

```console
 $ lein uberjar
 Saving project dependencies in resources/deps.txt as text.
 Created /usr/home/your_account/some/project.dir/target/your_project-0.1.1-SNAPSHOT.jar
 $ jar tf /usr/home/your_account/some/project.dir/target/your_project-0.1.0-SNAPSHOT.jar | fgrep deps.txt
 deps.txt
 $
```


## Check

Beside saving your dependencies `record-deps` can also check them and
make sure, for instance, that the version of an old dependency is not
accidentally changed by a new dependency.

You just need to specify the `:check` option in your prep-tasks

```clojure
:prep-tasks [["record-deps" :check true] "javac" "compile"]
```

or, if you want to choose a non-default name for the EDN file:

```clojure
:prep-tasks [["record-deps" :check "resources/the_dependencies.edn"] "javac" "compile"]
```

The compilation would then look like

```console
 $ lein compile
 No dependency version change.
 Compiling [...]
```

or like

```console
 $ lein compile
 Dependency version change detected; if this is intended delete resources/deps.edn
 old:
     [io.netty/netty 3.10.6.Final]
 new:
     [io.netty/netty 3.7.0.Final]
 Compiling [...]
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
