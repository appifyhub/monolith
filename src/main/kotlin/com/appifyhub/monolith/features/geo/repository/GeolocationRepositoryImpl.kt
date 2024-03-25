package com.appifyhub.monolith.features.geo.repository

import com.appifyhub.monolith.features.geo.domain.model.Geolocation
import com.appifyhub.monolith.features.geo.storage.GeolocationDao
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class GeolocationRepositoryImpl(
  private val geolocationDao: GeolocationDao,
) : GeolocationRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun fetchGeolocationForIp(ipAddress: String?): Geolocation? {
    log.debug("Fetching geolocation for IP $ipAddress")
    return try {
      ipAddress?.let { geolocationDao.fetchGeolocationForIp(it) }
    } catch (t: Throwable) {
      log.warn("Failed to find IP", t)
      null
    }
  }

}
