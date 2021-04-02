package com.appifyhub.monolith.network.auth

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TokenDetailsResponse(
  @JsonProperty("user_id") val ownerId: String,
  @JsonProperty("project_id") val ownerProjectId: Long,
  @JsonProperty("universal_id") val ownerUniversalId: String,
  @JsonProperty("token_id") val tokenId: String,
  @JsonProperty("is_blocked") val isBlocked: Boolean,
  @JsonProperty("origin") val origin: String?,
  @JsonProperty("created_at") val createdAt: String,
  @JsonProperty("expires_at") val expiresAt: String,
) : Serializable
