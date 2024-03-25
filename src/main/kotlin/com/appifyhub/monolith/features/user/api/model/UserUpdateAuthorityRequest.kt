package com.appifyhub.monolith.features.user.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class UserUpdateAuthorityRequest(
  @JsonProperty("authority") val authority: String,
) : Serializable
