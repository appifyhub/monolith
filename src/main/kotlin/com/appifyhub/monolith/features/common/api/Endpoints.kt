package com.appifyhub.monolith.features.common.api

object Endpoints {

  /* ********************* USERS ********************** */

  // Authentication

  const val USER_AUTH = "/v1/universal/auth"
  const val USER_TOKENS = "/v1/universal/auth/tokens"

  // Users

  const val PROJECT_SIGNUP = "/v1/projects/{projectId}/signup"
  const val PROJECT_USER_SEARCH = "/v1/projects/{projectId}/search"
  const val UNIVERSAL_USER = "/v1/universal/users/{universalId}"
  const val UNIVERSAL_USER_AUTHORITY = "/v1/universal/users/{universalId}/authority"
  const val UNIVERSAL_USER_DATA = "/v1/universal/users/{universalId}/data"
  const val UNIVERSAL_USER_SIGNATURE = "/v1/universal/users/{universalId}/signature"
  const val UNIVERSAL_USER_SIGNATURE_RESET = "/v1/universal/users/{universalId}/signature/reset"
  const val UNIVERSAL_USER_VERIFY = "/v1/universal/users/{universalId}/verify/{verificationToken}"
  const val UNIVERSAL_USER_PUSH_DEVICE = "/v1/universal/users/{universalId}/push-devices/{deviceId}"
  const val UNIVERSAL_USER_PUSH_DEVICES = "/v1/universal/users/{universalId}/push-devices"
  const val UNIVERSAL_USER_SIGNUP_CODES = "/v1/universal/users/{universalId}/signup-codes"

  /* ******************** CREATORS ******************** */

  // Authentication

  const val CREATOR_AUTH = "/v1/creator/auth"
  const val CREATOR_API_KEY = "/v1/creator/apikey"

  // Projects

  const val PROJECTS = "/v1/projects"
  const val PROJECT = "/v1/projects/{projectId}"

  // Users

  const val CREATOR_SIGNUP = "/v1/creator/signup"
  const val UNIVERSAL_USER_FORCE_VERIFY = "/v1/universal/users/{universalId}/force-verify"

  // Templating and messaging

  const val TEMPLATES = "/v1/projects/{projectId}/messaging/template"
  const val TEMPLATE = "/v1/projects/{projectId}/messaging/templates/{templateId}"
  const val TEMPLATE_SEARCH = "/v1/projects/{projectId}/messaging/template-search"
  const val TEMPLATE_VARIABLES = "/v1/projects/{projectId}/messaging/template-variables"
  const val TEMPLATE_MATERIALIZE = "/v1/projects/{projectId}/messaging/template-materialize"
  const val MESSAGING_SEND = "/v1/projects/{projectId}/users/{universalId}/message"

  /* ***************** UNAUTHENTICATED ****************** */

  const val ERROR = "/error"
  const val FAVICON = "/favicon*"
  const val FAVICON_DIR = "/favicon/**"
  const val HEARTBEAT = "/heartbeat"
  const val H2_CONSOLE = "/h2-console/**" // shows up in debug mode only
  const val SIGNUP_USERS = "/v1/projects/*/signup" // see PROJECT_SIGNUP
  const val SIGNUP_CREATORS = CREATOR_SIGNUP
  const val VERIFICATION = "/v1/universal/users/*/verify/*" // see UNIVERSAL_USER_VERIFY
  const val SIGNATURE_RESET = "/v1/universal/users/*/signature/reset" // see UNIVERSAL_USER_SIGNATURE_RESET
  const val DOCS = "/docs/**"
  const val ACTUATOR = "/actuator/**"

}
