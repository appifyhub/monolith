package com.appifyhub.monolith.service.admin

import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.domain.admin.property.Property.DecimalProp
import com.appifyhub.monolith.domain.admin.property.Property.FlagProp
import com.appifyhub.monolith.domain.admin.property.Property.IntegerProp
import com.appifyhub.monolith.domain.admin.property.Property.StringProp
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_FLAG
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.GENERIC_INTEGER
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.PROJECT_DESCRIPTION
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.PROJECT_LOGO_URL
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.PROJECT_USERS_MAX
import com.appifyhub.monolith.network.admin.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.admin.PropertyRepository
import com.appifyhub.monolith.util.AuthTestHelper
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
import java.time.temporal.ChronoUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class PropertyServiceImplTest {

  @Autowired lateinit var service: PropertyService
  @Autowired lateinit var adminRepo: AdminRepository
  @Autowired lateinit var propRepo: PropertyRepository
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var authHelper: AuthTestHelper

  private val adminProject: Project by lazy { adminRepo.getAdminProject() }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
    propRepo.clearAllProperties(adminProject)
  }

  @Test fun `getting configurations filtered fails with invalid filter`() {
    val filter = PropertyFilterQueryParams(type = "invalid")
    assertThat {
      service.getConfigurationsFiltered(filter)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Property filter is invalid")
      }
  }

  @Test fun `getting configurations filtered succeeds`() {
    val filter = PropertyFilterQueryParams(name_contains = PROJECT_LOGO_URL.name.lowercase())
    assertThat(service.getConfigurationsFiltered(filter))
      .isEqualTo(listOf(PROJECT_LOGO_URL))
  }

  @Test fun `fetching property fails with invalid project ID`() {
    assertThat {
      service.fetchProperty<Any>(-1, "invalid")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `fetching property fails with missing project ID`() {
    assertThat {
      service.fetchProperty<Any>(20, "invalid")
    }
      .isFailure()
      .hasClass(NoSuchElementException::class)
  }

  @Test fun `fetching property fails with invalid prop name`() {
    assertThat {
      service.fetchProperty<Any>(adminProject.id, "invalid")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("not found")
      }
  }

  @Test fun `fetching property succeeds`() {
    val propStub = IntegerProp(GENERIC_INTEGER, adminProject.id, "20", timeProvider.currentDate)
    val prop = propRepo.saveProperty(adminProject, propStub)

    assertThat(
      service.fetchProperty<Int>(adminProject.id, GENERIC_INTEGER.name)
        .cleanStubArtifacts()
    )
      .isEqualTo(prop.cleanStubArtifacts())
  }

  @Test fun `fetching property list fails with invalid prop name`() {
    assertThat {
      service.fetchProperties(adminProject.id, listOf(GENERIC_INTEGER.name, "invalid"))
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("not found")
      }
  }

  @Test fun `fetching property list succeeds`() {
    val propStub1 = IntegerProp(GENERIC_INTEGER, adminProject.id, "20", timeProvider.currentDate)
    val propStub2 = FlagProp(GENERIC_FLAG, adminProject.id, "true", timeProvider.currentDate)
    val prop1 = propRepo.saveProperty(adminProject, propStub1)
    val prop2 = propRepo.saveProperty(adminProject, propStub2)

    assertThat(
      service.fetchProperties(adminProject.id, listOf(GENERIC_INTEGER.name, GENERIC_FLAG.name))
        .map { it.cleanStubArtifacts() }
    )
      .isEqualTo(listOf(prop1, prop2).map { it.cleanStubArtifacts() })
  }

  @Test fun `fetching property list filtered fails with invalid filter`() {
    val filter = PropertyFilterQueryParams(type = "invalid")
    assertThat {
      service.fetchPropertiesFiltered(adminProject.id, filter)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Property filter is invalid")
      }
  }

  @Test fun `fetching property list filtered succeeds`() {
    val propStub = IntegerProp(PROJECT_LOGO_URL, adminProject.id, "url", timeProvider.currentDate)
    val prop = propRepo.saveProperty(adminProject, propStub)
    val filter = PropertyFilterQueryParams(name_contains = PROJECT_LOGO_URL.name.lowercase())

    assertThat(
      service.fetchPropertiesFiltered(adminProject.id, filter)
        .map { it.cleanStubArtifacts() }
    )
      .isEqualTo(listOf(prop).map { it.cleanStubArtifacts() })
  }

  @Test fun `saving property fails with invalid value`() {
    assertThat {
      service.saveProperty<Any>(adminProject.id, PROJECT_USERS_MAX.name, "abc")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Property")
      }
  }

  @Test fun `saving property succeeds`() {
    val prop = IntegerProp(PROJECT_USERS_MAX, adminProject.id, "100", timeProvider.currentDate)
    assertThat(
      service.saveProperty<Int>(adminProject.id, PROJECT_USERS_MAX.name, "100")
        .cleanStubArtifacts()
    )
      .isDataClassEqualTo(prop.cleanStubArtifacts())
  }

  @Test fun `saving property list fails with mismatching sizes`() {
    assertThat {
      service.saveProperties(adminProject.id, listOf(PROJECT_USERS_MAX.name), emptyList())
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Property names need to correspond")
      }
  }

  @Test fun `saving property list fails with invalid value`() {
    assertThat {
      service.saveProperties(
        projectId = adminProject.id,
        propNames = listOf(PROJECT_USERS_MAX.name, PROJECT_DESCRIPTION.name),
        propRawValues = listOf("100", "\t\n"),
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Property")
      }
  }

  @Test fun `saving property list succeeds`() {
    val prop1 = IntegerProp(PROJECT_USERS_MAX, adminProject.id, "100", timeProvider.currentDate)
    val prop2 = StringProp(PROJECT_DESCRIPTION, adminProject.id, "description", timeProvider.currentDate)

    assertThat(
      service.saveProperties(
        projectId = adminProject.id,
        propNames = listOf(PROJECT_USERS_MAX.name, PROJECT_DESCRIPTION.name),
        propRawValues = listOf("100", "description"),
      ).map { it.cleanStubArtifacts() }
    )
      .isEqualTo(listOf(prop1, prop2).map { it.cleanStubArtifacts() })
  }

  @Test fun `clearing property succeeds`() {
    val propStub = IntegerProp(PROJECT_USERS_MAX, adminProject.id, "100", timeProvider.currentDate)
    propRepo.saveProperty(adminProject, propStub)

    assertAll {
      // can find it at first
      assertThat {
        service.fetchProperty<Any>(adminProject.id, PROJECT_USERS_MAX.name)
      }.isSuccess()

      // removing works
      assertThat {
        service.clearProperty(adminProject.id, PROJECT_USERS_MAX.name)
      }.isSuccess()

      // can't find it anymore
      assertThat {
        service.fetchProperty<Any>(adminProject.id, PROJECT_USERS_MAX.name)
      }.isFailure()
    }
  }

  @Test fun `clearing property list filtered succeeds`() {
    val filter = PropertyFilterQueryParams(name_contains = "project")
    val propStub1 = IntegerProp(PROJECT_USERS_MAX, adminProject.id, "100", timeProvider.currentDate)
    val propStub2 = StringProp(PROJECT_DESCRIPTION, adminProject.id, "description", timeProvider.currentDate)
    propRepo.saveProperties(adminProject, listOf(propStub1, propStub2))

    assertAll {
      // can find them at first
      assertThat {
        service.fetchPropertiesFiltered(adminProject.id, filter)
      }
        .isSuccess()
        .transform { it.filter { prop -> prop.config in setOf(PROJECT_USERS_MAX, PROJECT_DESCRIPTION) }.size }
        .isEqualTo(2)

      // removing works
      assertThat {
        service.clearPropertiesFiltered(adminProject.id, filter)
      }.isSuccess()

      // can't find it anymore
      assertThat {
        service.fetchPropertiesFiltered(adminProject.id, filter)
      }
        .isSuccess()
        .isEqualTo(emptyList())
    }
  }

  @Test fun `fetching from different projects yields different results`() {
    val project1 = adminProject
    val propStub1 = IntegerProp(PROJECT_USERS_MAX, project1.id, "100", timeProvider.currentDate)
    val prop1 = propRepo.saveProperty(project1, propStub1)

    val project2 = authHelper.ensureProject(forceNewProject = true)
    val propStub2 = IntegerProp(PROJECT_USERS_MAX, project2.id, "200", timeProvider.currentDate)
    val prop2 = propRepo.saveProperty(project2, propStub2)

    assertAll {
      assertThat(
        service.fetchProperty<Int>(project1.id, PROJECT_USERS_MAX.name)
          .cleanStubArtifacts()
      ).isDataClassEqualTo(prop1.cleanStubArtifacts())

      assertThat(
        service.fetchProperty<Int>(project2.id, PROJECT_USERS_MAX.name)
          .cleanStubArtifacts()
      ).isDataClassEqualTo(prop2.cleanStubArtifacts())

      // cleaning up
      assertThat {
        service.clearPropertiesFiltered(project1.id, params = null)
        service.clearPropertiesFiltered(project2.id, params = null)
      }.isSuccess()
    }
  }

  // Helpers

  private fun <T : Any> Property<T>.cleanStubArtifacts() = when (this) {
    is DecimalProp -> copy(updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS))
    is FlagProp -> copy(updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS))
    is IntegerProp -> copy(updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS))
    is StringProp -> copy(updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS))
    else -> throw IllegalStateException("What is this type? ${this::class.simpleName}")
  }

}
