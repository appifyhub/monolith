package com.appifyhub.monolith.network.heartbeat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.Instant

@JsonInclude(Include.NON_NULL)
data class HeartbeatResponse(
  @JsonProperty("beat_time") val beat: Instant,
  @JsonProperty("request_ip") val ip: String?,
  @JsonProperty("request_geo") val geo: String?,
  @JsonProperty("version") val version: String,
) : Serializable
