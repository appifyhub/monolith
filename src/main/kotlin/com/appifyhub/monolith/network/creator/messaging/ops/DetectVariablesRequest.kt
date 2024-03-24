package com.appifyhub.monolith.network.creator.messaging.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class DetectVariablesRequest(
  @JsonProperty("content") val content: String,
) : Serializable
