package com.appifyhub.monolith.controller.common

object Endpoints {

  /* ********************* USERS ********************** */

  // Authentication

  const val AUTH = "/v1/universal/auth"
  const val TOKENS = "/v1/universal/auth/tokens"

  // User data

  const val ANY_PROJECT_SIGNUP = "/v1/projects/{projectId}/signup"
  const val ANY_PROJECT_SEARCH = "/v1/projects/{projectId}/search"
  const val ANY_USER_UNIVERSAL = "/v1/universal/users/{universalId}"
  const val ANY_USER_UNIVERSAL_AUTHORITY = "/v1/universal/users/{universalId}/authority"
  const val ANY_USER_UNIVERSAL_DATA = "/v1/universal/users/{universalId}/data"

  /* ******************** CREATORS ******************** */

  // Authentication

  const val CREATOR_AUTH = "/v1/creator/auth"
  const val CREATOR_API_KEY = "/v1/creator/apikey"
  const val ANY_USER_AUTH = "/v1/projects/{projectId}/users/{userId}/auth"
  const val ANY_USER_TOKENS = "/v1/projects/{projectId}/users/{userId}/auth/tokens"

  // Projects

  const val PROJECTS = "/v1/projects"
  const val ANY_PROJECT = "/v1/projects/{projectId}"

  // Properties

  const val CONFIGURATIONS = "/v1/projects/{projectId}/configurations"
  const val PROPERTIES = "/v1/projects/{projectId}/properties"
  const val PROPERTY = "/v1/projects/{projectId}/properties/{propertyName}"

  // Creator data

  const val ANY_USER = "/v1/projects/{projectId}/users/{userId}"

  /* ***************** UNAUTHENTICATED ****************** */

  const val ERROR = "/error"
  const val FAVICON = "/favicon**"
  const val HEARTBEAT = "/heartbeat"
  const val H2_CONSOLE = "/h2-console/**" // debug mode only
  const val SIGNUP_USERS = "/v1/projects/**/signup" // see ANY_USER_SIGNUP
  const val SIGNUP_CREATORS = "/v1/creator/signup"

}
