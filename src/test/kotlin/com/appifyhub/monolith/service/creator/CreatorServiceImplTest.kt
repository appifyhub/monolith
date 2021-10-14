package com.appifyhub.monolith.service.creator

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNotNull
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.repository.creator.CreatorRepository
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import java.time.temporal.ChronoUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.MethodMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class CreatorServiceImplTest {

  @Autowired lateinit var service: CreatorService
  @Autowired lateinit var creatorRepo: CreatorRepository
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var stubber: Stubber

  private val creatorProject: Project by lazy { creatorRepo.getCreatorProject() }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add a standard project fails if creator is null`() {
    assertThat {
      service.addProject(Stubs.projectCreator)
    }
      .isFailure()
      .messageContains("must be provided")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add a standard project fails if creator is not from creator project`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).owner()

    assertThat {
      service.addProject(Stubs.projectCreator.copy(owner = owner))
    }
      .isFailure()
      .messageContains("only by creator project users")
  }

  @Test fun `add creator project fails if creator is provided`() {
    // mocking because the real one already contains the creator project after setup
    val mockRepo = mock<CreatorRepository> {
      onGeneric { getCreatorProject() } doThrow UninitializedPropertyAccessException("Not initialized")
    }
    val service: CreatorService = CreatorServiceImpl(mockRepo)

    assertThat {
      service.addProject(Stubs.projectCreator.copy(owner = stubber.creators.owner()))
    }
      .isFailure()
      .messageContains("must not be provided")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add project succeeds with valid data (with creator)`() {
    assertThat(
      service.addProject(Stubs.projectCreator.copy(owner = stubber.creators.owner())).cleanDates()
    ).isDataClassEqualTo(
      Stubs.project.copy(
        id = Stubs.project.id + 1,
      ).cleanStubArtifacts()
    )
  }

  @Test fun `get creator project succeeds`() {
    assertThat(service.getCreatorProject())
      .isNotNull()
  }

  @Test fun `get creator owner succeeds`() {
    assertThat(service.getCreatorOwner())
      .isNotNull()
  }

  @Test fun `fetch project by ID fails with invalid account ID`() {
    assertThat {
      service.fetchProjectById(-1)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `fetch project by ID succeeds with valid account ID`() {
    assertThat(service.fetchProjectById(creatorProject.id))
      .isDataClassEqualTo(creatorProject)
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetch all projects by creator fails if creator's project is not creator project`() {
    val project = stubber.projects.new()
    val creator = stubber.users(project).owner()

    assertThat {
      service.fetchAllProjectsByCreator(creator)
    }
      .isFailure()
      .messageContains("don't have any projects")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetch all projects by creator succeeds`() {
    val project = stubber.projects.new()

    assertThat(service.fetchAllProjectsByCreator(stubber.creators.owner()).map { it.cleanDates() })
      .isEqualTo(listOf(project).map { it.cleanDates() })
  }

  @Test fun `fetch project creator fails with invalid project ID`() {
    assertThat {
      service.fetchProjectCreator(-1)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetch project creator succeeds`() {
    val project = stubber.projects.new()

    assertThat(service.fetchProjectCreator(project.id))
      .isDataClassEqualTo(stubber.creators.owner())
  }

  @Test fun `update project fails with invalid project ID`() {
    assertThat {
      service.updateProject(Stubs.projectUpdater.copy(id = -1))
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `update project succeeds with valid data`() {
    val project = stubber.projects.new().cleanStubArtifacts()

    assertThat(
      service.updateProject(Stubs.projectUpdater.copy(id = project.id)).cleanStubArtifacts()
    ).isDataClassEqualTo(
      Stubs.projectUpdated.copy(id = project.id).cleanStubArtifacts()
    )
  }

  @Test fun `remove project by ID fails with invalid project ID`() {
    assertThat {
      service.removeProjectById(-1)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `remove project by ID fails for creator project`() {
    assertThat {
      service.removeProjectById(stubber.projects.creator().id)
    }
      .isFailure()
      .messageContains("Creator project can't")
  }

  @Test fun `remove project by ID fails with creator project ID`() {
    assertThat {
      service.removeProjectById(creatorProject.id)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Creator project")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `remove project by ID succeeds with valid data`() {
    val project = stubber.projects.new()
    stubber.users(project).admin()
    stubber.users(project).default()

    assertThat {
      service.removeProjectById(project.id)
    }.isSuccess()
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `remove all projects by creator fails with user outside of creator project`() {
    val project = stubber.projects.new()
    val creator = stubber.users(project).owner()

    assertThat {
      service.removeAllProjectsByCreator(creator)
    }
      .isFailure()
      .messageContains("only by creator project users")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `remove all projects by creator succeeds`() {
    stubber.projects.new()

    assertThat {
      service.removeAllProjectsByCreator(stubber.creators.owner())
    }.isSuccess()
  }

  // Helpers

  // leftovers from hacking time provider need to be removed
  private fun Project.cleanStubArtifacts(): Project = copy(
    createdAt = timeProvider.currentDate,
    updatedAt = timeProvider.currentDate,
  ).cleanDates()

  private fun Project.cleanDates(): Project = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.DAYS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.DAYS),
  )

}
