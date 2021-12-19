package com.appifyhub.monolith.repository.geo

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.appifyhub.monolith.storage.dao.GeolocationDao
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

class GeolocationRepositoryImplTest {

  private val geolocationDao = mock<GeolocationDao>()

  private val repository: GeolocationRepository = GeolocationRepositoryImpl(
    geolocationDao = geolocationDao,
  )

  @Test fun `repository returns null when IP is null`() {
    assertThat(repository.fetchGeolocationForIp(null))
      .isNull()
  }

  @Test fun `repository returns null when dao throws`() {
    geolocationDao.stub {
      on { fetchGeolocationForIp(any()) } doThrow IllegalArgumentException("failed")
    }

    assertThat(repository.fetchGeolocationForIp("5.4.3.2"))
      .isNull()
  }

  @Test fun `repository returns null when dao has a result`() {
    geolocationDao.stub {
      on { fetchGeolocationForIp(any()) } doReturn Stubs.geo
    }

    assertThat(repository.fetchGeolocationForIp("5.4.3.2"))
      .isNotNull()
      .isDataClassEqualTo(Stubs.geo)
  }

}
