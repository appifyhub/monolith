package com.appifyhub.monolith.network.creator.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class ProjectCreateRequest(
  @JsonProperty("type") val type: String,
  @JsonProperty("user_id_type") val userIdType: String,
  @JsonProperty("owner_universal_id") val ownerUniversalId: String,
)
