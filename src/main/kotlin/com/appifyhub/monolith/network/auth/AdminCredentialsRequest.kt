package com.appifyhub.monolith.network.auth

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class AdminCredentialsRequest(
  @JsonProperty("user_id") val userId: String,
  @JsonProperty("secret") val secret: String,
  @JsonProperty("origin") val origin: String? = null,
) : Serializable