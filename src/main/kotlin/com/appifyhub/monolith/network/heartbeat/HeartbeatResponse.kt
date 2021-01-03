package com.appifyhub.monolith.network.heartbeat

import java.io.Serializable
import java.time.Instant

data class HeartbeatResponse(
  val responseTime: Instant,
) : Serializable