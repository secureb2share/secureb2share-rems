(ns rems.context
  "Collection of the global variables for REMS.

   When referring, please make your use greppable with the prefix context,
   i.e. context/*root-path*.")

(def ^:dynamic *root-path*
  "Application root path also known as context-path.

  If application does not live at '/',
  then this is the path before application relative paths.")

(def ^:dynamic *user*
  "User data available from request.")

