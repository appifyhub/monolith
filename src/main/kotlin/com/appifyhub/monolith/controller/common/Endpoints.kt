package com.appifyhub.monolith.controller.common

object Endpoints {

  // Unauthenticated

  const val ERROR = "/error"
  const val FAVICON = "/favicon**"
  const val HEARTBEAT = "/heartbeat"
  const val H2_CONSOLE = "/h2-console/**" // debug mode only

  // Authentication

  const val AUTH = "/v1/universal/auth"
  const val TOKENS = "/v1/universal/auth/tokens"

  // User data

  const val ONE_USER = "/v1/universal/users/{universalId}"

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

  /* endregion */

}
