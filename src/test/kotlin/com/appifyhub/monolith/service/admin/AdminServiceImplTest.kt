package com.appifyhub.monolith.service.admin

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
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.repository.admin.AdminRepository
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
class AdminServiceImplTest {

  @Autowired lateinit var service: AdminService
  @Autowired lateinit var adminRepo: AdminRepository
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var stubber: Stubber

  private val adminProject: Project by lazy { adminRepo.getAdminProject() }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add a standard project fails if creator is null`() {
    assertThat {
      service.addProject(Stubs.projectCreator, null)
    }
      .isFailure()
      .messageContains("must be provided")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add a standard project fails if creator is not from admin project`() {
    val project = stubber.projects.new()
    val creator = stubber.users(project).owner()

    assertThat {
      service.addProject(Stubs.projectCreator, creator)
    }
      .isFailure()
      .messageContains("only by admin project users")
  }

  @Test fun `add admin project fails if creator is provided`() {
    // mocking because the real one already contains the admin project after setup
    val mockRepo = mock<AdminRepository> {
      onGeneric { getAdminProject() } doThrow UninitializedPropertyAccessException("Not initialized")
    }
    val service: AdminService = AdminServiceImpl(mockRepo)

    assertThat {
      service.addProject(Stubs.projectCreator, stubber.creators.owner())
    }
      .isFailure()
      .messageContains("must not be provided")
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add project succeeds with valid data (with creator)`() {
    assertThat(
      service.addProject(Stubs.projectCreator, stubber.creators.owner()).cleanDates()
    ).isDataClassEqualTo(
      Stubs.project.copy(
        id = Stubs.project.id + 1,
      ).cleanStubArtifacts()
    )
  }

  @Test fun `get admin project succeeds`() {
    assertThat(service.getAdminProject())
      .isNotNull()
  }

  @Test fun `get admin owner succeeds`() {
    assertThat(service.getAdminOwner())
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
    assertThat(service.fetchProjectById(adminProject.id))
      .isDataClassEqualTo(adminProject)
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetch all projects by creator fails if creator's project is not admin project`() {
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

  @Test fun `remove project by ID fails for admin project`() {
    assertThat {
      service.removeProjectById(stubber.projects.creator().id)
    }
      .isFailure()
      .messageContains("Admin project can't")
  }

  @Test fun `remove project by ID fails with admin project ID`() {
    assertThat {
      service.removeProjectById(adminProject.id)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Admin project")
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
  @Test fun `remove all projects by creator fails with user outside of admin project`() {
    val project = stubber.projects.new()
    val creator = stubber.users(project).owner()

    assertThat {
      service.removeAllProjectsByCreator(creator)
    }
      .isFailure()
      .messageContains("only by admin project users")
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
