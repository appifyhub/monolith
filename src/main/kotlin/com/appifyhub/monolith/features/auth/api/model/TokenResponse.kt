package com.appifyhub.monolith.features.auth.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class TokenResponse(
  @JsonProperty("token_value") val tokenValue: String,
) : Serializable
