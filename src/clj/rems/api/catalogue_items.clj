(ns rems.api.catalogue-items
  (:require [compojure.api.sweet :refer :all]
            [rems.api.schema :refer :all]
            [rems.api.util :refer [check-user]]
            [rems.db.catalogue :as catalogue]
            [rems.db.core :as db]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema GetCatalogueItemsResponse
  [CatalogueItem])

;; TODO resid is misleading: it's the internal id, not the string id
;; Should we take the string id instead?
(s/defschema CreateCatalogueItemCommand
  {:title s/Str
   :form s/Num
   :resid s/Num
   :wfid s/Num})

(s/defschema CreateCatalogueItemResponse
  CatalogueItem)

(s/defschema CreateCatalogueItemLocalizationCommand
  {:id s/Num
   :langcode s/Str
   :title s/Str})

(s/defschema UpdateCatalogueItemCommand
  {:id s/Num
   :state (s/enum "disabled" "enabled")})

(def catalogue-items-api
  (context "/catalogue-items" []
    :tags ["catalogue items"]

    (GET "/" []
      :summary "Get catalogue items"
      :query-params [{resource :- (describe s/Str "resource id (optional)") nil}]
      :return GetCatalogueItemsResponse
      (check-user)
      (ok (catalogue/get-localized-catalogue-items {:resource resource})))

    (GET "/:item-id" []
      :summary "Get a single catalogue item"
      :path-params [item-id :- (describe s/Num "catalogue item")]
      :responses {200 {:schema CatalogueItem}
                  404 {:schema s/Str :description "Not found"}}

      (check-user)
      (if-let [it (catalogue/get-localized-catalogue-item item-id)]
        (ok it)
        (not-found! "not found")))

    (POST "/create" []
      :summary "Create a new catalogue item"
      :roles #{:owner}
      :body [command CreateCatalogueItemCommand]
      :return CreateCatalogueItemResponse
      (ok (catalogue/create-catalogue-item! command)))

    (PUT "/update" []
      :summary "Update catalogue item"
      :roles #{:owner}
      :body [command UpdateCatalogueItemCommand]
      :return SuccessResponse
      (db/set-catalogue-item-state! {:item (:id command) :state (:state command)})
      (ok {:success true}))

    (POST "/create-localization" []
      :summary "Create a new catalogue item localization"
      :roles #{:owner}
      :body [command CreateCatalogueItemLocalizationCommand]
      :return SuccessResponse
      (ok (catalogue/create-catalogue-item-localization! command)))))
