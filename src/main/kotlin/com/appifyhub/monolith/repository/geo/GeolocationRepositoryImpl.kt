package com.appifyhub.monolith.repository.geo

import com.appifyhub.monolith.domain.geo.Geolocation
import com.appifyhub.monolith.storage.dao.GeolocationDao
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class GeolocationRepositoryImpl(
  private val geolocationDao: GeolocationDao,
) : GeolocationRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun fetchGeolocationForIp(ipAddress: String): Geolocation? {
    log.debug("Fetching geolocation for IP $ipAddress")
    return try {
      geolocationDao.fetchGeolocationForIp(ipAddress)
    } catch (t: Throwable) {
      log.warn("Failed to find IP", t)
      null
    }
  }

}
