package com.appifyhub.monolith.storage.dao.impl

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.messageContains
import com.appifyhub.monolith.domain.geo.Geolocation
import com.appifyhub.monolith.storage.dao.GeolocationDao
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GeolocationDaoLocalFileTest {

  private val dao: GeolocationDao = GeolocationDaoLocalFile()

  @BeforeEach fun setup() {
    (dao as GeolocationDaoLocalFile).setup()
  }

  @AfterEach fun teardown() {
    (dao as GeolocationDaoLocalFile).teardown()
  }

  @Test fun `fetching blank IP throws`() {
    assertFailure { dao.fetchGeolocationForIp("\n \t") }
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("IP address is blank")
      }
  }

  @Test fun `fetching invalid IP throws`() {
    assertFailure { dao.fetchGeolocationForIp("invalid") }
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Invalid IP address")
      }
  }

  @Test fun `fetching missing IP throws`() {
    assertFailure { dao.fetchGeolocationForIp("255.255.255.255") }
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("IP geolocation not found")
      }
  }

  @Test fun `fetching USA IP works`() {
    assertThat(dao.fetchGeolocationForIp(Stubs.ipAddress))
      .isDataClassEqualTo(Stubs.geo)
  }

  @Test fun `fetching German IP works`() {
    assertThat(dao.fetchGeolocationForIp("95.90.246.175"))
      .isDataClassEqualTo(
        Geolocation(
          countryCode = "DE",
          countryName = "Germany",
          region = "Berlin",
          city = "Berlin",
        ),
      )
  }

}
