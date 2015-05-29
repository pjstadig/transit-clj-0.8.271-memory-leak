(ns transit-clj-memory-leak.core
  (:require
   [cognitect.transit :as transit]
   [trptcolin.versioneer.core :as version]))

(defn transit-encode
  [obj]
  (let [out (java.io.ByteArrayOutputStream.)]
    (transit/write (transit/writer out :msgpack) obj)
    (.toByteArray out)))

(defn -main
  ([]
     (-main nil))
  ([memoize?]
     (println "Max Heap:" (.maxMemory (Runtime/getRuntime)))
     (println "transit-clj Version:"
              (version/get-version "com.cognitect" "transit-clj"))
     (when memoize?
       (alter-var-root #'transit/default-write-handlers memoize))
     (let [maps (repeatedly 20000 #(hash-map :random (rand-int 1000000)))]
       (doseq [chunk (partition-all 1000 maps)]
         (let [start (System/currentTimeMillis)
               _ (doseq [obj chunk]
                   (transit-encode obj))
               duration (- (System/currentTimeMillis) start)]
           (println "Encoded chunk of" (count chunk)
                    (str "(" duration " ms)")))))))
