package com.appifyhub.monolith.service.auth

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isFailure
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority.ADMIN
import com.appifyhub.monolith.domain.user.User.Authority.DEFAULT
import com.appifyhub.monolith.domain.user.User.Authority.MODERATOR
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.auth.AccessManager.Privilege.PROJECT_READ
import com.appifyhub.monolith.service.auth.AccessManager.Privilege.PROJECT_WRITE
import com.appifyhub.monolith.service.auth.AccessManager.Privilege.USER_READ
import com.appifyhub.monolith.service.auth.AccessManager.Privilege.USER_WRITE
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
import java.time.Duration
import java.time.temporal.ChronoUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AccessManagerImplTest {

  @Autowired lateinit var manager: AccessManager
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var stubber: Stubber

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  // region User access

  @Test fun `requesting user access with invalid user ID fails`() {
    assertThat {
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(OWNER),
        targetId = UserId("", stubber.projects.creator().id),
        privilege = USER_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `requesting user access fails with expired token`() {
    val token = stubber.creatorTokens().real(OWNER)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      manager.requestUserAccess(
        authData = token,
        targetId = stubber.creators.owner().id,
        privilege = USER_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `requesting user access fails with requesting READ for superiors`() {
    val project = stubber.projects.new()
    val targetAdmin = stubber.users(project).admin()
    assertThat {
      manager.requestUserAccess(
        authData = stubber.tokens(project).real(MODERATOR),
        targetId = targetAdmin.id,
        privilege = USER_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only ${targetAdmin.authority.nextGroupName} are authorized")
      }
  }

  @Test fun `requesting user access fails with requesting WRITE for superiors`() {
    val project = stubber.projects.new()
    val targetOwner = stubber.users(project).owner()
    assertThat {
      manager.requestUserAccess(
        authData = stubber.tokens(project).real(ADMIN),
        targetId = targetOwner.id,
        privilege = USER_WRITE,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only ${targetOwner.authority.nextGroupName} are authorized")
      }
  }

  @Test fun `requesting user access fails with mismatching projects (in standard projects)`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).owner()
    assertThat {
      manager.requestUserAccess(
        authData = stubber.tokens(owner).real(isStatic = true),
        targetId = stubber.creators.default().id,
        privilege = USER_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only requests within the same project are allowed")
      }
  }

  @Test fun `requesting user access fails with mismatching projects (in creators project)`() {
    assertThat {
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(DEFAULT, isStatic = true),
        targetId = stubber.creators.owner().id,
        privilege = USER_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only ${USER_READ.level.groupName} are authorized")
      }
  }

  @Test fun `requesting user access succeeds with requesting READ for self`() {
    assertThat(
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(DEFAULT),
        targetId = stubber.creators.default().id,
        privilege = USER_READ,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(stubber.creators.default().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting WRITE for self`() {
    assertThat(
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(OWNER),
        targetId = stubber.creators.owner().id,
        privilege = USER_WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(stubber.creators.owner().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting READ for inferiors`() {
    assertThat(
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(OWNER),
        targetId = stubber.creators.default().id,
        privilege = USER_READ,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(stubber.creators.default().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting WRITE for inferiors`() {
    assertThat(
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(OWNER),
        targetId = stubber.creators.default().id,
        privilege = USER_WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(stubber.creators.default().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with creator requesting WRITE for inferiors`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(creator)
    assertThat(
      manager.requestUserAccess(
        authData = stubber.tokens(creator).real(),
        targetId = stubber.users(project).default().id,
        privilege = USER_WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(stubber.users(project).default().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting WRITE for another owner with static token`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).owner()
    val targetOwner = stubber.users(project).owner(idSuffix = "_another")
    assertThat(
      manager.requestUserAccess(
        authData = stubber.tokens(owner).real(isStatic = true),
        targetId = targetOwner.id,
        privilege = USER_WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(targetOwner.cleanStubArtifacts())
  }

  // endregion

  // region Project access

  @Test fun `requesting project access with invalid project ID fails`() {
    assertThat {
      manager.requestProjectAccess(
        authData = stubber.creatorTokens().real(OWNER),
        targetId = -1,
        privilege = PROJECT_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `requesting project access fails with expired token`() {
    val token = stubber.creatorTokens().real(OWNER)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      manager.requestProjectAccess(
        authData = token,
        targetId = stubber.creators.owner().id.projectId,
        privilege = PROJECT_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `requesting project access fails with mismatching projects (in standard projects)`() {
    val project = stubber.projects.new()
    assertThat {
      manager.requestProjectAccess(
        authData = stubber.tokens(project).real(OWNER, isStatic = true),
        targetId = stubber.projects.creator().id,
        privilege = PROJECT_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only requests within the same project are allowed")
      }
  }

  @Test fun `requesting project access fails with mismatching projects (in creators project)`() {
    assertThat {
      manager.requestProjectAccess(
        authData = stubber.creatorTokens().real(DEFAULT, isStatic = true),
        targetId = stubber.projects.creator().id,
        privilege = PROJECT_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only ${PROJECT_READ.level.groupName} are authorized")
      }
  }

  @Test fun `requesting project access fails with insufficient READ privileges`() {
    assertThat {
      manager.requestProjectAccess(
        authData = stubber.creatorTokens().real(DEFAULT),
        targetId = stubber.creators.owner().id.projectId,
        privilege = PROJECT_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only owners are authorized")
      }
  }

  @Test fun `requesting project access fails with insufficient WRITE privileges`() {
    assertThat {
      manager.requestProjectAccess(
        authData = stubber.creatorTokens().real(ADMIN),
        targetId = stubber.creators.owner().id.projectId,
        privilege = PROJECT_WRITE,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only owners are authorized")
      }
  }

  @Test fun `requesting project access succeeds with requesting READ`() {
    val project = stubber.projects.new()
    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(project).real(OWNER),
        targetId = project.id,
        privilege = PROJECT_READ,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  @Test fun `requesting project access succeeds with requesting WRITE (static token)`() {
    val project = stubber.projects.new()
    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(project).real(OWNER, isStatic = true),
        targetId = project.id,
        privilege = PROJECT_WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  @Test fun `requesting project access succeeds with requesting WRITE (non-static token)`() {
    val project = stubber.projects.new()
    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(project).real(OWNER),
        targetId = project.id,
        privilege = PROJECT_WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  @Test fun `requesting project access succeeds with creator requesting WRITE`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(creator = creator)
    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(creator).real(),
        targetId = project.id,
        privilege = PROJECT_WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  // endregion

  // region Helpers

  // leftovers from hacking in auth utils need to be removed
  private fun User.cleanStubArtifacts() = copy(
    createdAt = timeProvider.currentDate,
    verificationToken = null,
  ).cleanDates()

  private fun User.cleanDates() = copy(
    birthday = birthday?.truncateTo(ChronoUnit.DAYS),
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS),
  )

  private fun Project.cleanStubArtifacts(): Project = copy(
    createdAt = timeProvider.currentDate,
    updatedAt = timeProvider.currentDate,
  ).cleanDates()

  private fun Project.cleanDates(): Project = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.DAYS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.DAYS),
  )

  // endregion

}
