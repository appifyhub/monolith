package com.appifyhub.monolith.controller.heartbeat

import com.appifyhub.monolith.controller.common.RequestIpAddressHolder
import com.appifyhub.monolith.controller.heartbeat.HeartbeatController.Endpoints.HEARTBEAT
import com.appifyhub.monolith.domain.mapper.mergeToString
import com.appifyhub.monolith.network.heartbeat.HeartbeatResponse
import com.appifyhub.monolith.repository.geo.GeolocationRepository
import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.util.meta.BuildMetadata
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HeartbeatController(
  private val timeProvider: TimeProvider,
  private val buildMetadata: BuildMetadata,
  private val geoRepo: GeolocationRepository,
) : RequestIpAddressHolder {

  object Endpoints {
    const val HEARTBEAT = "/heartbeat"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(HEARTBEAT)
  fun beat(): HeartbeatResponse {
    log.debug("[GET] heartbeat")

    val ipAddress = getRequestIpAddress()

    return HeartbeatResponse(
      beat = timeProvider.currentInstant,
      ip = ipAddress,
      geo = geoRepo.fetchGeolocationForIp(ipAddress)?.mergeToString(),
      version = with(buildMetadata) { "$version.$quality" },
    )
  }

}
