package com.appifyhub.monolith.service.admin

import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isZero
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.util.AuthTestHelper
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
import java.time.temporal.ChronoUnit
import javax.annotation.Resource
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
  @Autowired lateinit var authHelper: AuthTestHelper

  // for some reason this injection is problematic
  @Resource(name = "userRepositoryImpl")
  lateinit var userRepo: UserRepository

  private val adminProject: Project by lazy { adminRepo.getAdminProject() }
  private val adminAccount: Account by lazy {
    // this way it fetches also owner data (it's not contained in project.account)
    adminRepo.fetchAccountById(adminProject.account.id)
  }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `add project fails with invalid account ID`() {
    assertThat {
      service.addProject(
        Stubs.projectCreator.copy(
          account = Stubs.account.copy(id = -1),
        ),
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Account ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add project succeeds with valid data`() {
    assertThat(
      service.addProject(
        Stubs.projectCreator.copy(account = adminAccount)
      ).cleanDates()
    ).isDataClassEqualTo(
      Stubs.project.copy(
        id = 3,
        account = adminAccount.copy(owners = emptyList()), // no ownership data here
      ).cleanStubArtifacts()
    )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `add account succeeds`() {
    assertThat(service.addAccount())
      .isDataClassEqualTo(
        Account(
          id = 3,
          owners = emptyList(),
          createdAt = timeProvider.currentDate,
        )
      )
  }

  @Test fun `get admin project succeeds`() {
    assertThat(service.getAdminProject())
      .isDataClassEqualTo(adminProject)
  }

  @Test fun `fetch account by ID fails with invalid account ID`() {
    assertThat {
      service.fetchAccountById(-1)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Account ID")
      }
  }

  @Test fun `fetch account by ID succeeds with valid account ID`() {
    assertThat(service.fetchAccountById(adminAccount.id))
      .isDataClassEqualTo(adminAccount)
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

  @Test fun `fetch all projects by account fails with invalid account ID`() {
    assertThat {
      service.fetchAllProjectsByAccount(Stubs.account.copy(id = -1))
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Account ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetch all projects by account succeeds with valid account ID`() {
    val account = service.addAccount()
    val project1 = service.addProject(Stubs.projectCreator.copy(account = account)).cleanStubArtifacts()
    val project2 = service.addProject(Stubs.projectCreator.copy(account = account)).cleanStubArtifacts()

    assertThat(
      service.fetchAllProjectsByAccount(account).map { it.cleanStubArtifacts() }
    )
      .isEqualTo(listOf(project1, project2))
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

  @Test fun `update project fails with invalid account ID`() {
    assertThat {
      service.updateProject(
        Stubs.projectUpdater.copy(
          account = Settable(Stubs.account.copy(id = -1)),
        )
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Account ID")
      }
  }

  @Test fun `update project succeeds with valid data`() {
    val account = service.addAccount()
    val project = service.addProject(Stubs.projectCreator.copy(account = account)).cleanStubArtifacts()

    assertThat(
      service.updateProject(
        Stubs.projectUpdater.copy(
          id = project.id,
          account = Settable(adminAccount),
        )
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(
      Stubs.projectUpdated.copy(
        id = project.id,
        account = adminAccount.copy(owners = emptyList()), // no ownership data here,
      ).cleanStubArtifacts()
    )
  }

  @Test fun `update account fails with invalid account ID`() {
    assertThat {
      service.updateAccount(Stubs.accountUpdater.copy(id = -1))
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Account ID")
      }
  }

  @Test fun `update account fails with invalid added user ID`() {
    assertThat {
      service.updateAccount(
        Stubs.accountUpdater.copy(
          addedOwners = Settable(
            listOf(
              Stubs.user.copy(id = UserId("\t\n", -1)),
            )
          )
        )
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Added User ID")
      }
  }

  @Test fun `update account fails with invalid removed user ID`() {
    assertThat {
      service.updateAccount(
        Stubs.accountUpdater.copy(
          removedOwners = Settable(
            listOf(
              Stubs.user.copy(id = UserId("\t\n", -1)),
            )
          )
        )
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Removed User ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `update account succeeds with valid data`() {
    val account = service.addAccount()
    val randomModerator = authHelper.moderatorUser
    val randomModeratorUpdated = randomModerator.copy(account = account)
    val randomAdmin = authHelper.adminUser
    val randomAdminUpdated = randomAdmin.copy(account = account)

    assertAll {
      // adding owners
      assertThat(
        service.updateAccount(
          AccountUpdater(
            id = account.id,
            addedOwners = Settable(listOf(randomModerator)),
          )
        ).cleanDates()
      ).isDataClassEqualTo(
        account.copy(
          owners = listOf(randomModeratorUpdated),
        ).cleanDates()
      )

      // removing owners and adding new
      assertThat(
        service.updateAccount(
          AccountUpdater(
            id = account.id,
            addedOwners = Settable(listOf(randomAdmin)),
            removedOwners = Settable(listOf(randomModeratorUpdated)),
          )
        ).cleanDates()
      ).isDataClassEqualTo(
        account.copy(
          owners = listOf(randomAdminUpdated),
        ).cleanDates()
      )
    }
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
    val account = service.addAccount().cleanDates()
    val project = service.addProject(Stubs.projectCreator.copy(account = account)).cleanStubArtifacts()
    userRepo.addUser(
      creator = Stubs.userCreator.copy(projectId = project.id),
      userIdType = project.userIdType,
    )

    assertAll {
      assertThat(userRepo.fetchAllUsersByProjectId(project.id).size)
        .isEqualTo(1)
      assertThat(service.fetchAllProjectsByAccount(account).size)
        .isEqualTo(1)

      service.removeProjectById(project.id)

      assertThat(userRepo.fetchAllUsersByProjectId(project.id).size)
        .isZero()
      assertThat(service.fetchAllProjectsByAccount(account).size)
        .isZero()
    }
  }

  @Test fun `remove account by ID fails with invalid account ID`() {
    assertThat {
      service.removeAccountById(-1)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Account ID")
      }
  }

  @Test fun `remove account by ID fails with admin account ID`() {
    assertThat {
      service.removeAccountById(adminAccount.id)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Admin account")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `remove account by ID succeeds with valid data`() {
    val account = service.addAccount().cleanDates()
    val project = service.addProject(Stubs.projectCreator.copy(account = account)).cleanStubArtifacts()
    val user = userRepo.addUser(
      creator = Stubs.userCreator.copy(projectId = project.id),
      userIdType = project.userIdType,
    )
    service.updateAccount(
      AccountUpdater(
        id = account.id,
        addedOwners = Settable(listOf(user)),
      )
    )

    assertAll {
      assertThat(service.fetchAccountById(account.id).id)
        .isEqualTo(account.id)
      assertThat(userRepo.fetchAllUsersByProjectId(project.id).size)
        .isEqualTo(1)
      assertThat(service.fetchAllProjectsByAccount(account).size)
        .isEqualTo(1)

      service.removeAccountById(account.id)

      assertThat(userRepo.fetchAllUsersByProjectId(project.id).size)
        .isZero()
      assertThat(service.fetchAllProjectsByAccount(account).size)
        .isZero()
      assertThat { service.fetchAccountById(account.id) }
        .isFailure()
        .hasClass(NoSuchElementException::class)
    }
  }

  // Helpers

  // leftovers from hacking time provider need to be removed
  private fun Project.cleanStubArtifacts(): Project = copy(
    createdAt = timeProvider.currentDate,
    updatedAt = timeProvider.currentDate,
  ).cleanDates()

  private fun Project.cleanDates(): Project = copy(
    account = account.cleanDates(),
    createdAt = createdAt.truncateTo(ChronoUnit.DAYS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.DAYS),
  )

  private fun Account.cleanDates(): Account = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.DAYS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.DAYS),
    owners = owners.map { it.cleanDates() },
  )

  private fun User.cleanDates(): User = copy(
    birthday = birthday?.truncateTo(ChronoUnit.DAYS),
    createdAt = createdAt.truncateTo(ChronoUnit.DAYS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.DAYS),
    account = account?.cleanDates(),
  )

}
