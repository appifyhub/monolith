package com.appifyhub.monolith.features.common.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class SettableRequest<out T>(
  @JsonProperty("value") val value: T,
) : Serializable
