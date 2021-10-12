package com.appifyhub.monolith.network.creator.property

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class PropertyResponse(
  @JsonProperty("configuration") val config: PropertyConfigurationResponse,
  @JsonProperty("raw_value") val rawValue: String,
  @JsonProperty("updated_at") val updatedAt: String,
)
