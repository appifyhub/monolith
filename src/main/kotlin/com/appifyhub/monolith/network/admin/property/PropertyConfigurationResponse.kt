package com.appifyhub.monolith.network.admin.property

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class PropertyConfigurationResponse(
  @JsonProperty("name") val name: String,
  @JsonProperty("type") val type: String,
  @JsonProperty("category") val category: String,
  @JsonProperty("tags") val tags: Set<String>,
  @JsonProperty("default_value") val defaultValue: String,
  @JsonProperty("is_mandatory") val isMandatory: Boolean,
  @JsonProperty("is_secret") val isSecret: Boolean,
  @JsonProperty("is_deprecated") val isDeprecated: Boolean,
)
