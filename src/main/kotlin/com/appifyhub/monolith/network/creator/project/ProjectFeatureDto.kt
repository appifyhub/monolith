package com.appifyhub.monolith.network.creator.project

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ProjectFeatureDto(
  @JsonProperty("name") val name: String,
  @JsonProperty("is_required") val isRequired: Boolean,
  @JsonProperty("related_properties") val properties: List<String>,
) : Serializable
