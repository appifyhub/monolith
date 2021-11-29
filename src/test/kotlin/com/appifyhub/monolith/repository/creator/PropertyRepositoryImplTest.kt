package com.appifyhub.monolith.repository.creator

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.storage.dao.PropertyDao
import com.appifyhub.monolith.storage.model.creator.PropertyDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import java.util.Date
import java.util.Optional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PropertyRepositoryImplTest {

  private val propertyDao = mock<PropertyDao>()
  private val timeProvider = TimeProviderFake()

  private val repository = PropertyRepositoryImpl(
    propertyDao = propertyDao,
    timeProvider = timeProvider,
  )

  @Suppress("UNCHECKED_CAST")
  @BeforeEach fun setup() {
    propertyDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as PropertyDbm }
      onGeneric { saveAll(any<Iterable<PropertyDbm>>()) } doAnswer { it.arguments.first() as List<PropertyDbm> }
    }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `fetch property uses the DAO to fetch`() {
    propertyDao.stub {
      onGeneric { findById(Stubs.propStringIdDbm) } doReturn Optional.of(Stubs.propStringDbm)
    }

    assertThat(repository.fetchProperty<String>(Stubs.project, Stubs.propString.config))
      .isDataClassEqualTo(Stubs.propString)
  }

  @Test fun `fetch properties uses the DAO to fetch`() {
    propertyDao.stub {
      onGeneric {
        findAllByIdIn(
          listOf(
            Stubs.propStringIdDbm,
            Stubs.propIntegerIdDbm,
          )
        )
      } doReturn listOf(Stubs.propStringDbm, Stubs.propIntegerDbm)
    }

    assertThat(repository.fetchProperties(Stubs.project, listOf(Stubs.propString.config, Stubs.propInteger.config)))
      .isEqualTo(listOf(Stubs.propString, Stubs.propInteger))
  }

  @Test fun `fetch all properties uses the DAO to fetch`() {
    propertyDao.stub {
      onGeneric { findAllById_ProjectId(Stubs.project.id) } doReturn listOf(Stubs.propStringDbm, Stubs.propIntegerDbm)
    }

    assertThat(repository.fetchAllProperties(Stubs.project))
      .isEqualTo(listOf(Stubs.propString, Stubs.propInteger))
  }

  @Test fun `save property uses the DAO to store`() {
    timeProvider.staticTime = { 0xFFFFFF }

    assertThat(repository.saveProperty(Stubs.project, Stubs.propString))
      .isDataClassEqualTo(Stubs.propString.copy(updatedAt = Date(0xFFFFFF)))
  }

  @Test fun `save properties uses the DAO to store`() {
    timeProvider.staticTime = { 0xFFFFFF }

    assertThat(repository.saveProperties(Stubs.project, listOf(Stubs.propString, Stubs.propInteger)))
      .isEqualTo(
        listOf(
          Stubs.propString.copy(updatedAt = Date(0xFFFFFF)),
          Stubs.propInteger.copy(updatedAt = Date(0xFFFFFF)),
        )
      )
  }

  @Test fun `clear property (domain model) uses the DAO to clear`() {
    repository.clearProperty(Stubs.propString)

    verify(propertyDao)
      .deleteById(Stubs.propStringIdDbm)
  }

  @Test fun `clear property (by config) uses the DAO to clear`() {
    repository.clearProperty(Stubs.propString)

    verify(propertyDao)
      .deleteById(Stubs.propStringIdDbm)
  }

  @Test fun `clear properties (domain model) uses the DAO to clear`() {
    repository.clearProperties(listOf(Stubs.propString, Stubs.propInteger))

    verify(propertyDao)
      .deleteAllByIdIn(listOf(Stubs.propStringIdDbm, Stubs.propIntegerIdDbm))
  }

  @Test fun `clear properties (by config) uses the DAO to clear`() {
    repository.clearProperties(Stubs.project, listOf(Stubs.propString.config, Stubs.propInteger.config))

    verify(propertyDao)
      .deleteAllByIdIn(listOf(Stubs.propStringIdDbm, Stubs.propIntegerIdDbm))
  }

  @Test fun `clear all properties uses the DAO to clear`() {
    repository.clearAllProperties(Stubs.project)

    verify(propertyDao)
      .deleteAllById_ProjectId(Stubs.project.id)
  }

}
