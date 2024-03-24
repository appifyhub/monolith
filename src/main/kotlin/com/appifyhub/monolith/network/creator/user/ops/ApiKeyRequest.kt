package com.appifyhub.monolith.network.creator.user.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ApiKeyRequest(
  @JsonProperty("origin") val origin: String? = null,
) : Serializable
