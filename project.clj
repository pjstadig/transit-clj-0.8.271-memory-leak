(defproject transit-clj-0.8.271-memory-leak "0.1.0-SNAPSHOT"
  :description "Demonstration of the memory leak in transit-clj 0.8.271"
  :url "https://github.com/pjstadig/transit-clj-0.8.271-memory-leak"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [trptcolin/versioneer "0.2.0"]
                 [com.cognitect/transit-clj "0.8.271"]]
  :main ^:skip-aot transit-clj-memory-leak.core
  :profiles {:transit-clj-0.8.269
             {:dependencies ^:replace [[org.clojure/clojure "1.6.0"]
                                       [trptcolin/versioneer "0.2.0"]
                                       [com.cognitect/transit-clj "0.8.269"]]}}
  :jvm-opts ["-Xmx50m" "-Xms50m"])
