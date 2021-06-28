package com.appifyhub.monolith.network.admin.property.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class MultiplePropertiesSaveRequest(
  @JsonProperty("properties") val properties: List<PropertyDto>,
)
