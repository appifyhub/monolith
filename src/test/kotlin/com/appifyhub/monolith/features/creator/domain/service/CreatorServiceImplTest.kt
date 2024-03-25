package com.appifyhub.monolith.features.creator.domain.service

import assertk.all
import assertk.assertAll
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.features.common.domain.model.Settable
import com.appifyhub.monolith.features.common.domain.model.mapValueNullable
import com.appifyhub.monolith.eventbus.ProjectCreated
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.repository.CreatorRepository
import com.appifyhub.monolith.util.EventBusFake
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.extension.truncateTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.MethodMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class CreatorServiceImplTest {

  @Autowired lateinit var service: CreatorService
  @Autowired lateinit var creatorRepo: CreatorRepository
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var eventBus: EventBusFake
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
    assertFailure {
      service.addProject(Stubs.projectCreator)
    }
      .messageContains("must be provided")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add a standard project fails if creator is not from creator project`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).owner()

    assertFailure {
      service.addProject(Stubs.projectCreator.copy(owner = owner))
    }
      .messageContains("only by creator project users")
  }

  @Test fun `add creator project fails if creator is provided`() {
    // mocking because the real one already contains the creator project after setup
    val mockRepo = mock<CreatorRepository> {
      onGeneric { getCreatorProject() } doThrow UninitializedPropertyAccessException("Not initialized")
    }
    val service: CreatorService = CreatorServiceImpl(
      creatorRepository = mockRepo,
      eventPublisher = eventBus,
    )

    assertFailure {
      service.addProject(Stubs.projectCreator.copy(owner = stubber.creators.owner()))
    }
      .messageContains("must not be provided")
  }

  @Test fun `add project fails with invalid name`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      name = "\n\t",
      logoUrl = null,
      websiteUrl = null,
    )

    assertFailure { service.addProject(projectData) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project's Name")
      }
  }

  @Test fun `add project fails with invalid max users`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      maxUsers = -10,
      logoUrl = null,
      websiteUrl = null,
    )

    assertFailure { service.addProject(projectData) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project's Max Users")
      }
  }

  @Test fun `add project fails with invalid requires max signup codes per user`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      maxSignupCodesPerUser = -10,
      logoUrl = null,
      websiteUrl = null,
    )

    assertFailure { service.addProject(projectData) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project's Max Signup Codes Per User")
      }
  }

  @Test fun `add project fails with invalid mailgun config`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      logoUrl = null,
      websiteUrl = null,
      mailgunConfig = Stubs.projectCreator.mailgunConfig?.copy(senderEmail = "invalid"),
    )

    assertFailure { service.addProject(projectData) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Mailgun Config")
      }
  }

  @Test fun `add project fails with invalid twilio config`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      logoUrl = null,
      websiteUrl = null,
      twilioConfig = Stubs.projectCreator.twilioConfig?.copy(defaultSenderNumber = "invalid"),
    )

    assertFailure { service.addProject(projectData) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Twilio Config")
      }
  }

  @Test fun `add project fails with invalid firebase config`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      logoUrl = null,
      websiteUrl = null,
      firebaseConfig = Stubs.projectCreator.firebaseConfig?.copy(serviceAccountKeyJsonBase64 = "base!64"),
    )

    assertFailure { service.addProject(projectData) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Firebase Config")
      }
  }

  @Test fun `add project succeeds with invalid description`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      description = "\n\t",
      logoUrl = null,
      websiteUrl = null,
    )

    assertThat(service.addProject(projectData))
      .isInstanceOf(Project::class)
  }

  @Test fun `add project succeeds with invalid logo URL`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      logoUrl = "\n\t",
      websiteUrl = null,
    )

    assertThat(service.addProject(projectData))
      .isInstanceOf(Project::class)
  }

  @Test fun `add project succeeds with invalid website URL`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      websiteUrl = "\n\t",
      logoUrl = null,
    )

    assertThat(service.addProject(projectData))
      .isInstanceOf(Project::class)
  }

  @Test fun `add project succeeds with invalid language tag`() {
    val owner = stubber.creators.default()
    val projectData = Stubs.projectCreator.copy(
      owner = owner,
      languageTag = "asdasdasdasdasd",
      logoUrl = null,
      websiteUrl = null,
    )

    assertThat(service.addProject(projectData))
      .isInstanceOf(Project::class)
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add project succeeds with valid data (with creator)`() {
    val projectData = Stubs.projectCreator.copy(
      owner = stubber.creators.owner(),
      logoUrl = "https://www.example.com/logo.png",
      websiteUrl = "https://www.example.com",
    )

    assertAll {
      val expectedProject = Stubs.project.copy(
        id = 5, // there are other stubs using the same incrementor
        logoUrl = "https://www.example.com/logo.png",
        websiteUrl = "https://www.example.com",
      ).cleanStubArtifacts()
      val expectedEvent = ProjectCreated(
        ownerProject = creatorProject,
        payload = expectedProject,
      )

      assertThat(service.addProject(projectData).cleanDates())
        .isDataClassEqualTo(expectedProject)

      assertThat(eventBus.lastPublished)
        .transform { it as ProjectCreated }
        .isDataClassEqualTo(expectedEvent)
    }
  }

  @Test fun `get creator project succeeds`() {
    assertThat(service.getCreatorProject())
      .isNotNull()
  }

  @Test fun `get creator owner succeeds`() {
    assertThat(service.getSuperCreator())
      .isNotNull()
  }

  @Test fun `fetching all creator projects succeeds`() {
    assertThat(service.fetchAllProjects())
      .hasSize(1)
  }

  @Test fun `fetch project by ID fails with invalid account ID`() {
    assertFailure {
      service.fetchProjectById(-1)
    }
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

    assertFailure {
      service.fetchAllProjectsByCreator(creator)
    }
      .messageContains("don't have any projects")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetch all projects by creator succeeds`() {
    val project = stubber.projects.new()

    assertThat(service.fetchAllProjectsByCreator(stubber.creators.owner()).map { it.cleanDates() })
      .isEqualTo(listOf(project).map { it.cleanDates() })
  }

  @Test fun `fetch project creator fails with invalid project ID`() {
    assertFailure {
      service.fetchProjectCreator(-1)
    }
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
    assertFailure {
      service.updateProject(Stubs.projectUpdater.copy(id = -1))
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `update project fails with invalid max users`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      maxUsers = Settable(-10),
      logoUrl = null,
      websiteUrl = null,
    )

    assertFailure { service.updateProject(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project's Max Users")
      }
  }

  @Test fun `update project fails with invalid max signup codes per user`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      maxSignupCodesPerUser = Settable(-10),
      logoUrl = null,
      websiteUrl = null,
    )

    assertFailure { service.updateProject(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project's Max Signup Codes Per User")
      }
  }

  @Test fun `update project fails with invalid name`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      name = Settable("\n\t"),
      logoUrl = null,
      websiteUrl = null,
    )

    assertFailure { service.updateProject(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project's Name")
      }
  }

  @Test fun `update project fails with invalid mailgun config`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      logoUrl = null,
      websiteUrl = null,
      mailgunConfig = Stubs.projectUpdater.mailgunConfig?.mapValueNullable {
        it.copy(senderEmail = "invalid")
      },
    )

    assertFailure { service.updateProject(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Mailgun Config")
      }
  }

  @Test fun `update project fails with invalid twilio config`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      logoUrl = null,
      websiteUrl = null,
      twilioConfig = Stubs.projectUpdater.twilioConfig?.mapValueNullable {
        it.copy(defaultSenderNumber = "invalid")
      },
    )

    assertFailure { service.updateProject(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Twilio Config")
      }
  }

  @Test fun `update project fails with invalid firebase config`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      logoUrl = null,
      websiteUrl = null,
      firebaseConfig = Stubs.projectUpdater.firebaseConfig?.mapValueNullable {
        it.copy(serviceAccountKeyJsonBase64 = "base!64")
      },
    )

    assertFailure { service.updateProject(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Firebase Config")
      }
  }

  @Test fun `update project succeeds with invalid description`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      description = Settable("\n\t"),
      logoUrl = null,
      websiteUrl = null,
    )

    assertThat(service.updateProject(updater))
      .isInstanceOf(Project::class)
  }

  @Test fun `update project succeeds with invalid logo URL`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      logoUrl = Settable("\n\t"),
      websiteUrl = null,
    )

    assertThat(service.updateProject(updater))
      .isInstanceOf(Project::class)
  }

  @Test fun `update project succeeds with invalid website URL`() {
    val project = stubber.projects.new()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      websiteUrl = Settable("\n\t"),
      logoUrl = null,
    )

    assertThat(service.updateProject(updater))
      .isInstanceOf(Project::class)
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `update project succeeds with an invalid language tag`() {
    val project = stubber.projects.new().cleanStubArtifacts()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      languageTag = Settable("asdasdasdasdasd"),
      logoUrl = null,
      websiteUrl = null,
    )

    assertThat(service.updateProject(updater))
      .isInstanceOf(Project::class)
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `update project succeeds with valid data`() {
    val project = stubber.projects.new().cleanStubArtifacts()
    val updater = Stubs.projectUpdater.copy(
      id = project.id,
      logoUrl = Settable("https://www.example1.com/logo1.png"),
      websiteUrl = Settable("https://www.example1.com"),
    )

    assertThat(
      service.updateProject(updater).cleanStubArtifacts(),
    ).isDataClassEqualTo(
      Stubs.projectUpdated.copy(
        id = project.id,
        logoUrl = "https://www.example1.com/logo1.png",
        websiteUrl = "https://www.example1.com",
      ).cleanStubArtifacts(),
    )
  }

  @Test fun `remove project by ID fails with invalid project ID`() {
    assertFailure {
      service.removeProjectById(-1)
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `remove project by ID fails for creator project`() {
    assertFailure {
      service.removeProjectById(stubber.projects.creator().id)
    }
      .messageContains("Creator project can't")
  }

  @Test fun `remove project by ID fails with creator project ID`() {
    assertFailure {
      service.removeProjectById(creatorProject.id)
    }
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

    assertThat(service.removeProjectById(project.id))
      .isEqualTo(Unit)
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `remove all projects by creator fails with user outside of creator project`() {
    val project = stubber.projects.new()
    val creator = stubber.users(project).owner()

    assertFailure {
      service.removeAllProjectsByCreator(creator.id)
    }
      .messageContains("only by creator project users")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `remove all projects by creator succeeds`() {
    stubber.projects.new()

    assertThat(service.removeAllProjectsByCreator(stubber.creators.owner().id))
      .isEqualTo(Unit)
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
