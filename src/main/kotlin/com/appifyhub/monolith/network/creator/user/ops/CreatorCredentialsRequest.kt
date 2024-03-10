package com.appifyhub.monolith.network.creator.user.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class CreatorCredentialsRequest(
  @JsonProperty("universal_id") val universalId: String,
  @JsonProperty("signature") val signature: String,
  @JsonProperty("origin") val origin: String? = null,
) : Serializable
