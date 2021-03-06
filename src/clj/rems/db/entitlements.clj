(ns rems.db.entitlements
  "Creating and fetching entitlements."
  (:require [cheshire.core :as cheshire]
            [clj-http.client :as http]
            [clojure.string :refer [join]]
            [clojure.tools.logging :as log]
            [rems.auth.util :refer [throw-forbidden]]
            [rems.config :refer [env]]
            [rems.db.core :as db]
            [rems.roles :refer [has-roles?]]
            [rems.text :as text]
            [rems.util :refer [getx-user-id]]))

;; TODO move Entitlement schema here from rems.api?

(defn- entitlement-to-api [{:keys [resid catappid start mail]}]
  {:resource resid
   :application-id catappid
   :start (text/localize-time start)
   :mail mail})

(defn get-entitlements-for-api [user-or-nil resource-or-nil]
  (if (has-roles? :approver)
    (mapv entitlement-to-api (db/get-entitlements {:user user-or-nil
                                                   :resource resource-or-nil}))
    (mapv entitlement-to-api (db/get-entitlements {:user (getx-user-id)
                                                   :resource resource-or-nil}))))

(defn get-entitlements-for-export
  "Returns a CSV string representing entitlements"
  []
  (when-not (has-roles? :approver)
    (throw-forbidden))
  (let [ents (db/get-entitlements)
        separator (:csv-separator env)]
    (with-out-str
      (println (join separator ["resource" "application" "user" "start"]))
      (doseq [e ents]
        (println (join separator [(:resid e) (:catappid e) (:userid e) (text/localize-time (:start e))]))))))

(defn- post-entitlements [target-key entitlements]
  (when-let [target (get-in env [:entitlements-target target-key])]
    (let [payload (for [e entitlements]
                    {:application (:catappid e)
                     :resource (:resid e)
                     :user (:userid e)
                     :mail (:mail e)})
          json-payload (cheshire/generate-string payload)]
      (log/infof "Posting entitlements to %s:" target payload)
      (let [response (try
                       (http/post target
                                  {:throw-exceptions false
                                   :body json-payload
                                   :content-type :json
                                   :socket-timeout 2500
                                   :conn-timeout 2500})
                       (catch Exception e
                         (log/error "POST failed" e)
                         {:status "exception"}))
            status (:status response)]
        (when-not (= 200 status)
          (log/warnf "Post failed: %s", response))
        (db/log-entitlement-post! {:target target :payload json-payload :status status})))))

(defn- add-entitlements-for
  "If the given application is approved, add an entitlement to the db
  and call the entitlement REST callback (if defined)."
  [application]
  (when (= "approved" (:state application))
    (db/add-entitlement! {:application (:id application)
                          :user (:applicantuserid application)})
    (post-entitlements :add (db/get-entitlements {:application (:id application)}))))

(defn- end-entitlements-for
  [application]
  (when (= "closed" (:state application))
    (db/end-entitlement! {:application (:id application)})
    (post-entitlements :remove (db/get-entitlements {:application (:id application)}))))

(defn update-entitlements-for
  [application]
  (add-entitlements-for application)
  (end-entitlements-for application))
