package com.appifyhub.monolith.network.messaging.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class PushDeviceRequest(
  @JsonProperty("id") val deviceId: String,
  @JsonProperty("type") val type: String,
) : Serializable
