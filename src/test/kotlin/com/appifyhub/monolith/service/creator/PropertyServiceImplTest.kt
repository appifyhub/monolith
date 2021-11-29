package com.appifyhub.monolith.service.creator

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
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.DESCRIPTION
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.GENERIC_FLAG
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.GENERIC_INTEGER
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.LOGO_URL
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.MAX_USERS
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.ON_HOLD
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.WEBSITE_URL
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.Property.DecimalProp
import com.appifyhub.monolith.domain.creator.property.Property.FlagProp
import com.appifyhub.monolith.domain.creator.property.Property.IntegerProp
import com.appifyhub.monolith.domain.creator.property.Property.StringProp
import com.appifyhub.monolith.network.creator.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.repository.creator.PropertyRepository
import com.appifyhub.monolith.util.Stubber
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
  @Autowired lateinit var propRepo: PropertyRepository
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var stubber: Stubber

  private val creatorProject: Project by lazy { stubber.projects.creator() }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
    propRepo.clearAllProperties(creatorProject)
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
    val filter = PropertyFilterQueryParams(name_contains = LOGO_URL.name.lowercase())
    assertThat(service.getConfigurationsFiltered(filter))
      .isEqualTo(listOf(LOGO_URL))
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
      service.fetchProperty<Any>(creatorProject.id, "invalid")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("not found")
      }
  }

  @Test fun `fetching property succeeds`() {
    val propStub = IntegerProp(GENERIC_INTEGER, creatorProject.id, "20", timeProvider.currentDate)
    val prop = propRepo.saveProperty(creatorProject, propStub)

    assertThat(
      service.fetchProperty<Int>(creatorProject.id, GENERIC_INTEGER.name)
        .cleanStubArtifacts()
    )
      .isEqualTo(prop.cleanStubArtifacts())
  }

  @Test fun `fetching property list fails with invalid prop name`() {
    assertThat {
      service.fetchProperties(creatorProject.id, listOf(GENERIC_INTEGER.name, "invalid"))
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("not found")
      }
  }

  @Test fun `fetching property list succeeds`() {
    val propStub1 = IntegerProp(GENERIC_INTEGER, creatorProject.id, "20", timeProvider.currentDate)
    val propStub2 = FlagProp(GENERIC_FLAG, creatorProject.id, "true", timeProvider.currentDate)
    val prop1 = propRepo.saveProperty(creatorProject, propStub1)
    val prop2 = propRepo.saveProperty(creatorProject, propStub2)

    assertThat(
      service.fetchProperties(creatorProject.id, listOf(GENERIC_INTEGER.name, GENERIC_FLAG.name))
        .map { it.cleanStubArtifacts() }
    )
      .isEqualTo(listOf(prop1, prop2).map { it.cleanStubArtifacts() })
  }

  @Test fun `fetching property list filtered fails with invalid filter`() {
    val filter = PropertyFilterQueryParams(type = "invalid")
    assertThat {
      service.fetchPropertiesFiltered(creatorProject.id, filter)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Property filter is invalid")
      }
  }

  @Test fun `fetching property list filtered succeeds`() {
    val propStub = IntegerProp(LOGO_URL, creatorProject.id, "url", timeProvider.currentDate)
    val prop = propRepo.saveProperty(creatorProject, propStub)
    val filter = PropertyFilterQueryParams(name_contains = LOGO_URL.name.lowercase())

    assertThat(
      service.fetchPropertiesFiltered(creatorProject.id, filter)
        .map { it.cleanStubArtifacts() }
    )
      .isEqualTo(listOf(prop).map { it.cleanStubArtifacts() })
  }

  @Test fun `saving property fails with invalid value`() {
    assertThat {
      service.saveProperty<Any>(creatorProject.id, MAX_USERS.name, "abc")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Property")
      }
  }

  @Test fun `saving property succeeds`() {
    val prop = IntegerProp(MAX_USERS, creatorProject.id, "100", timeProvider.currentDate)
    assertThat(
      service.saveProperty<Int>(creatorProject.id, MAX_USERS.name, "100")
        .cleanStubArtifacts()
    )
      .isDataClassEqualTo(prop.cleanStubArtifacts())
  }

  @Test fun `saving property list fails with mismatching sizes`() {
    assertThat {
      service.saveProperties(creatorProject.id, listOf(MAX_USERS.name), emptyList())
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
        projectId = creatorProject.id,
        propNames = listOf(MAX_USERS.name, DESCRIPTION.name),
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
    val prop1 = IntegerProp(MAX_USERS, creatorProject.id, "100", timeProvider.currentDate)
    val prop2 = StringProp(DESCRIPTION, creatorProject.id, "description", timeProvider.currentDate)

    assertThat(
      service.saveProperties(
        projectId = creatorProject.id,
        propNames = listOf(MAX_USERS.name, DESCRIPTION.name),
        propRawValues = listOf("100", "description"),
      ).map { it.cleanStubArtifacts() }
    )
      .isEqualTo(listOf(prop1, prop2).map { it.cleanStubArtifacts() })
  }

  @Test fun `clearing property fails when clearing mandatory property`() {
    val propStub = IntegerProp(ON_HOLD, creatorProject.id, "false", timeProvider.currentDate)
    propRepo.saveProperty(creatorProject, propStub)

    assertThat {
      service.clearProperty(creatorProject.id, ON_HOLD.name)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("is mandatory")
      }
  }

  @Test fun `clearing property succeeds`() {
    val propStub = IntegerProp(MAX_USERS, creatorProject.id, "100", timeProvider.currentDate)
    propRepo.saveProperty(creatorProject, propStub)

    assertAll {
      // can find it at first
      assertThat {
        service.fetchProperty<Any>(creatorProject.id, MAX_USERS.name)
      }.isSuccess()

      // removing works
      assertThat {
        service.clearProperty(creatorProject.id, MAX_USERS.name)
      }.isSuccess()

      // can't find it anymore
      assertThat {
        service.fetchProperty<Any>(creatorProject.id, MAX_USERS.name)
      }.isFailure()
    }
  }

  @Test fun `clearing property list filtered succeeds`() {
    val propStub1 = StringProp(LOGO_URL, creatorProject.id, "photo.com/1.png", timeProvider.currentDate)
    val propStub2 = StringProp(WEBSITE_URL, creatorProject.id, "website.com", timeProvider.currentDate)
    val propStub3 = FlagProp(ON_HOLD, creatorProject.id, "false", timeProvider.currentDate)
    propRepo.saveProperties(creatorProject, listOf(propStub1, propStub2, propStub3))
    val filter = PropertyFilterQueryParams(name_contains = "url")

    assertAll {
      // can find them at first
      assertThat {
        service.fetchPropertiesFiltered(creatorProject.id, filter)
      }
        .isSuccess()
        .transform {
          // ON_HOLD is skipped because it's mandatory
          it.filter { prop -> prop.config in setOf(LOGO_URL, WEBSITE_URL) }.size
        }
        .isEqualTo(2)

      // removing works
      assertThat {
        service.clearPropertiesFiltered(creatorProject.id, filter)
      }.isSuccess()

      // can't find it anymore
      assertThat {
        service.fetchPropertiesFiltered(creatorProject.id, filter)
      }
        .isSuccess()
        .isEqualTo(emptyList())
    }
  }

  @Test fun `fetching from different projects yields different results`() {
    val project1 = creatorProject
    val propStub1 = IntegerProp(MAX_USERS, project1.id, "100", timeProvider.currentDate)
    val prop1 = propRepo.saveProperty(project1, propStub1)

    val project2 = stubber.projects.new()
    val propStub2 = IntegerProp(MAX_USERS, project2.id, "200", timeProvider.currentDate)
    val prop2 = propRepo.saveProperty(project2, propStub2)

    assertAll {
      assertThat(
        service.fetchProperty<Int>(project1.id, MAX_USERS.name).cleanStubArtifacts()
      ).isDataClassEqualTo(prop1.cleanStubArtifacts())

      assertThat(
        service.fetchProperty<Int>(project2.id, MAX_USERS.name).cleanStubArtifacts()
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
