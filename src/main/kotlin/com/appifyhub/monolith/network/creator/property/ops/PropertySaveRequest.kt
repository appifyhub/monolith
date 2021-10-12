package com.appifyhub.monolith.network.creator.property.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class PropertySaveRequest(
  @JsonProperty("raw_value") val rawValue: String,
)
