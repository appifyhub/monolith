package com.appifyhub.monolith.network.auth

import java.io.Serializable

data class UserCredentialsRequest(
  val identifier: String,
  val secret: String,
  val origin: String? = null,
) : Serializable