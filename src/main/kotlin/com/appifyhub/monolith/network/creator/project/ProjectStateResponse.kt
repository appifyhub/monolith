package com.appifyhub.monolith.network.creator.project

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ProjectStateResponse(
  @JsonProperty("status") val status: String,
  @JsonProperty("usable_features") val usableFeatures: List<ProjectFeatureResponse>,
  @JsonProperty("unusable_features") val unusableFeatures: List<ProjectFeatureResponse>,
) : Serializable
