package com.appifyhub.monolith.network.heartbeat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.Instant

@JsonInclude(Include.NON_NULL)
data class HeartbeatResponse(
  @JsonProperty("request_processed_time") val beat: Instant,
) : Serializable