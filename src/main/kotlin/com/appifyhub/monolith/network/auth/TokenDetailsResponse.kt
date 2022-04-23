package com.appifyhub.monolith.network.auth

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class TokenDetailsResponse(
  @JsonProperty("token_value") val tokenValue: String,
  @JsonProperty("user_id") val ownerId: String,
  @JsonProperty("project_id") val ownerProjectId: Long,
  @JsonProperty("universal_id") val ownerUniversalId: String,
  @JsonProperty("created_at") val createdAt: String,
  @JsonProperty("expires_at") val expiresAt: String,
  @JsonProperty("authority") val authority: String,
  @JsonProperty("is_blocked") val isBlocked: Boolean,
  @JsonProperty("origin") val origin: String?,
  @JsonProperty("ip_address") val ipAddress: String?,
  @JsonProperty("geo") val geo: String?,
  @JsonProperty("is_static") val isStatic: Boolean,
) : Serializable
