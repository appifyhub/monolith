package com.appifyhub.monolith.features.creator.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ProjectFeatureResponse(
  @JsonProperty("name") val name: String,
  @JsonProperty("is_required") val isRequired: Boolean,
) : Serializable
