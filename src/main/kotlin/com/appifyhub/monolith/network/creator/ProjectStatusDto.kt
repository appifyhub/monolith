package com.appifyhub.monolith.network.creator

import com.appifyhub.monolith.network.creator.property.PropertyResponse
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ProjectStatusDto(
  @JsonProperty("status") val status: String,
  @JsonProperty("usable_features") val usableFeatures: List<ProjectFeatureDto>,
  @JsonProperty("unusable_features") val unusableFeatures: List<ProjectFeatureDto>,
  @JsonProperty("properties") val properties: List<PropertyResponse>,
) : Serializable
