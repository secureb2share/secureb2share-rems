(ns rems.catalogue-util
  (:require [clojure.string :as str]))

(defn urn-catalogue-item? [{:keys [resid]}]
  (and resid (str/starts-with? resid "urn:")))

(defn urn-catalogue-item-link [{:keys [resid]} {:keys [urn-organization]}]
  (str (or urn-organization "http://urn.fi/") resid))

(defn get-catalogue-item-title [item language]
  (or (get-in item [:localizations language :title])
      (:title item)))

(defn disabled-catalogue-item? [item]
  (= (:state item) "disabled"))
