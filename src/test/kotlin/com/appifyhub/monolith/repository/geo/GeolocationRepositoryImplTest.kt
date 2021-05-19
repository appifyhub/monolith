package com.appifyhub.monolith.repository.geo

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.appifyhub.monolith.storage.dao.GeolocationDao
import com.appifyhub.monolith.util.Stubs
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import org.junit.jupiter.api.Test

class GeolocationRepositoryImplTest {

  private val geolocationDao = mock<GeolocationDao>()

  private val repository: GeolocationRepository = GeolocationRepositoryImpl(
    geolocationDao = geolocationDao,
  )

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
