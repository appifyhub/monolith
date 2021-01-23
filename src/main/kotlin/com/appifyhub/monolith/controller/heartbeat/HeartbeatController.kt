package com.appifyhub.monolith.controller.heartbeat

import com.appifyhub.monolith.controller.heartbeat.HeartbeatController.Endpoints.HEARTBEAT
import com.appifyhub.monolith.network.heartbeat.HeartbeatResponse
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HeartbeatController(
  private val timeProvider: TimeProvider,
) {

  object Endpoints {
    const val HEARTBEAT = "/heartbeat"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(HEARTBEAT)
  fun beat(): HeartbeatResponse {
    log.debug("[GET] heartbeat")
    return HeartbeatResponse(timeProvider.currentInstant)
  }

}