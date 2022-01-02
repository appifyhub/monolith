package com.appifyhub.monolith.network.creator.project.ops

import com.appifyhub.monolith.network.common.SettableRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class ProjectUpdateRequest(
  @JsonProperty("type") val type: SettableRequest<String>? = null,
  @JsonProperty("status") val status: SettableRequest<String>? = null,
)
