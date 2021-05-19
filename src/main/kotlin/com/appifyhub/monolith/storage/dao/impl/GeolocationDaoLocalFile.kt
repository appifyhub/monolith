package com.appifyhub.monolith.storage.dao.impl

import com.appifyhub.monolith.domain.geo.Geolocation
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.storage.dao.GeolocationDao
import com.appifyhub.monolith.util.ext.takeIfNotBlank
import com.ip2location.IP2Location
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private const val CLASSPATH_LOCATIONS_FILE = "ip2location/IP2Location.bin"

@Component
class GeolocationDaoLocalFile : GeolocationDao {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Value("\${app.ip2location.file}")
  private var externalLocationsFile: String? = null

  private val ipLocation = IP2Location()

  @PostConstruct fun setup() {
    val filePath = externalLocationsFile?.takeIfNotBlank()?.trim()
      ?: Thread.currentThread()
        .contextClassLoader
        .getResource(CLASSPATH_LOCATIONS_FILE)!!
        .file

    ipLocation.Open(filePath)
  }

  @PreDestroy fun teardown() = try {
    ipLocation.Close()
  } catch (t: Throwable) {
    log.error("Failed to close IP locator", t)
  }

  override fun fetchGeolocationForIp(ipAddress: String): Geolocation {
    val result = ipLocation.IPQuery(ipAddress.trim())

    return when (result?.status) {
      "OK" -> requireNotNull(result.toDomain()) { "IP geolocation not found" }
      "EMPTY_IP_ADDRESS" -> throw IllegalArgumentException("IP address is blank")
      "INVALID_IP_ADDRESS" -> throw IllegalArgumentException("Invalid IP address")
      "MISSING_FILE" -> error("Invalid file")
      "IPV6_NOT_SUPPORTED" -> error("File does not contain IPv6 data")
      else -> error("Unknown error: " + result?.status.toString())
    }
  }

}
