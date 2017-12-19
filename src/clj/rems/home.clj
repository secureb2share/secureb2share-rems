(ns rems.home
  (:require [compojure.core :refer [GET defroutes]]
            [rems.auth.auth :as auth]
            [rems.context :as context]
            [rems.css.styles :as styles]
            [rems.guide :refer :all]
            [rems.layout :as layout]
            [rems.text :refer [text]]
            [ring.util.response :refer [content-type
                                        redirect
                                        response]]))

(defn- about [text]
  [:p text])

(defn- about-page []
  (layout/render
   "about"
   (about (text :t.about/text))))

(defn- home-page []
  (if context/*user*
    (redirect "/landing_page")
    (layout/render "home" (auth/login-component))))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (GET "/css/screen.css" [] (-> (styles/generate-css)
                                (response)
                                (content-type "text/css"))))

(defn guide
  "Component guide fragment"
  []
  (list
   (example "about" (about "Some about text"))))
