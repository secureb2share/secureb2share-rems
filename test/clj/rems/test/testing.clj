(ns rems.test.testing
  (:require [clojure.java.io :as io])
  (:import (ch.qos.logback.classic Level Logger)
           (com.google.common.io MoreFiles RecursiveDeleteOption)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)
           (org.slf4j LoggerFactory)))

(defn suppress-logging [^String logger-name]
  (fn [f]
    (let [^Logger logger (LoggerFactory/getLogger logger-name)
          original-level (.getLevel logger)]
      (.setLevel logger Level/OFF)
      (f)
      (.setLevel logger original-level))))

(defn create-temp-dir []
  (.toFile (Files/createTempDirectory (.toPath (io/file "target"))
                                      "test"
                                      (make-array FileAttribute 0))))

(defn delete-recursively [dir]
  (MoreFiles/deleteRecursively (.toPath (io/file dir))
                               (into-array [RecursiveDeleteOption/ALLOW_INSECURE])))
