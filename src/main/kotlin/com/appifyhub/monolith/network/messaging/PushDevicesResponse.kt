package com.appifyhub.monolith.network.messaging

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class PushDevicesResponse(
  @JsonProperty("devices") val devices: List<PushDeviceResponse>,
) : Serializable
