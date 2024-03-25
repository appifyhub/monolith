package com.appifyhub.monolith.features.heartbeat.api

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.controller.common.RequestIpAddressHolder
import com.appifyhub.monolith.features.geo.domain.mergeToString
import com.appifyhub.monolith.features.geo.repository.GeolocationRepository
import com.appifyhub.monolith.features.heartbeat.api.model.HeartbeatResponse
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

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(Endpoints.HEARTBEAT)
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
