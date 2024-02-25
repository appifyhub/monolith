package com.appifyhub.monolith.network.user

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class SignupCodeResponse(
  @JsonProperty("code") val code: String,
  @JsonProperty("is_used") val isUsed: Boolean,
  @JsonProperty("created_at") val createdAt: String,
  @JsonProperty("used_at") val usedAt: String?,
) : Serializable
