package com.appifyhub.monolith.service.auth

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.Project.Status
import com.appifyhub.monolith.domain.creator.setup.ProjectState
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority.ADMIN
import com.appifyhub.monolith.domain.user.User.Authority.DEFAULT
import com.appifyhub.monolith.domain.user.User.Authority.MODERATOR
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.access.AccessManager.Feature
import com.appifyhub.monolith.service.access.AccessManager.Privilege.PROJECT_READ
import com.appifyhub.monolith.service.access.AccessManager.Privilege.PROJECT_WRITE
import com.appifyhub.monolith.service.access.AccessManager.Privilege.USER_READ_DATA
import com.appifyhub.monolith.service.access.AccessManager.Privilege.USER_SEARCH
import com.appifyhub.monolith.service.access.AccessManager.Privilege.USER_WRITE_AUTHORITY
import com.appifyhub.monolith.service.access.AccessManager.Privilege.USER_WRITE_TOKEN
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
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
        privilege = USER_READ_DATA,
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
        privilege = USER_READ_DATA,
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
        privilege = USER_READ_DATA,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
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
        privilege = USER_WRITE_TOKEN,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
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
        privilege = USER_READ_DATA,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Only requests within the same project are allowed")
      }
  }

  @Test fun `requesting user access fails with mismatching projects (in creators project)`() {
    assertThat {
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(DEFAULT, isStatic = true),
        targetId = stubber.creators.owner().id,
        privilege = USER_READ_DATA,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Only ${USER_READ_DATA.level.groupName} are authorized")
      }
  }

  @Test fun `requesting user access fails when unverified`() {
    val project = stubber.projects.new()
    assertThat {
      manager.requestUserAccess(
        authData = stubber.tokens(project).real(OWNER, isStatic = true, autoVerified = false),
        targetId = stubber.users(project).default().id,
        privilege = USER_READ_DATA,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("not verified")
      }
  }

  @Test fun `requesting user access succeeds with requesting READ for self`() {
    assertThat(
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(DEFAULT),
        targetId = stubber.creators.default().id,
        privilege = USER_READ_DATA,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(stubber.creators.default().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting WRITE for self`() {
    assertThat(
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(OWNER),
        targetId = stubber.creators.owner().id,
        privilege = USER_WRITE_TOKEN,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(stubber.creators.owner().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting READ for inferiors`() {
    assertThat(
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(OWNER),
        targetId = stubber.creators.default().id,
        privilege = USER_READ_DATA,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(stubber.creators.default().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting WRITE for inferiors`() {
    assertThat(
      manager.requestUserAccess(
        authData = stubber.creatorTokens().real(OWNER),
        targetId = stubber.creators.default().id,
        privilege = USER_WRITE_TOKEN,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(stubber.creators.default().cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with creator requesting WRITE for inferiors`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(creator)
    assertThat(
      manager.requestUserAccess(
        authData = stubber.tokens(creator).real(),
        targetId = stubber.users(project).default().id,
        privilege = USER_WRITE_TOKEN,
      ).cleanStubArtifacts(),
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
        privilege = USER_WRITE_TOKEN,
      ).cleanStubArtifacts(),
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
        hasClass(ResponseStatusException::class)
        messageContains("Only requests within the same project are allowed")
      }
  }

  @Test fun `requesting project access fails when creator is unverified`() {
    val project = stubber.projects.new()
    assertThat {
      manager.requestProjectAccess(
        authData = stubber.tokens(project).real(OWNER, isStatic = true, autoVerified = false),
        targetId = project.id,
        privilege = PROJECT_READ,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("not verified")
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
        hasClass(ResponseStatusException::class)
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
        hasClass(ResponseStatusException::class)
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
        hasClass(ResponseStatusException::class)
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
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  @Test fun `requesting project access succeeds with requesting WRITE (static token)`() {
    val project = stubber.projects.new()
    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(project).real(OWNER, isStatic = true),
        targetId = project.id,
        privilege = PROJECT_WRITE,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  @Test fun `requesting project access succeeds with requesting WRITE (non-static token)`() {
    val project = stubber.projects.new()
    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(project).real(OWNER),
        targetId = project.id,
        privilege = PROJECT_WRITE,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  @Test fun `requesting project access succeeds with creator requesting WRITE`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator)
    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(creator).real(),
        targetId = project.id,
        privilege = PROJECT_WRITE,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  // endregion

  // region Creator access

  @Test fun `requesting creator fails with expired token`() {
    val token = stubber.creatorTokens().real(OWNER)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      manager.requestCreator(token, matchesId = null, requireVerified = false)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `requesting creator fails with mismatching project`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real()

    assertThat {
      manager.requestCreator(token, matchesId = null, requireVerified = false)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("from creators are allowed")
      }
  }

  @Test fun `requesting creator fails with mismatching user ID`() {
    val creator1 = stubber.creators.default()
    val creator2 = stubber.creators.default(idSuffix = "_other")
    val token = stubber.tokens(creator1).real()

    assertThat {
      manager.requestCreator(token, matchesId = creator2.id, requireVerified = false)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("${creator2.id.toUniversalFormat()} are allowed")
      }
  }

  @Test fun `requesting creator fails with unverified user`() {
    val creator = stubber.creators.default(autoVerified = false)
    val token = stubber.tokens(creator).real()

    assertThat {
      manager.requestCreator(token, matchesId = creator.id, requireVerified = true)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("must be verified")
      }
  }

  @Test fun `requesting creator succeeds with matching requester`() {
    val creator = stubber.creators.default()
    val token = stubber.tokens(creator).real()

    assertThat(
      manager.requestCreator(token, matchesId = creator.id, requireVerified = true)
        .cleanDates(),
    ).isDataClassEqualTo(creator.cleanDates())
  }

  @Test fun `requesting creator succeeds with no match required`() {
    val creator = stubber.creators.default()
    val token = stubber.tokens(creator).real()

    assertThat(
      manager.requestCreator(token, matchesId = null, requireVerified = true)
        .cleanDates(),
    ).isDataClassEqualTo(creator.cleanDates())
  }

  @Test fun `requesting creator succeeds with super-creator requester`() {
    val superCreator = stubber.creators.owner()
    val creator = stubber.creators.default()
    val token = stubber.tokens(superCreator).real()

    assertThat(
      manager.requestCreator(token, matchesId = creator.id, requireVerified = false)
        .cleanDates(),
    ).isDataClassEqualTo(creator.cleanDates())
  }

  // endregion

  // region Super creator access

  @Test fun `requesting super creator fails with expired token`() {
    val token = stubber.creatorTokens().real(OWNER)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      manager.requestSuperCreator(token)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `requesting super creator fails with mismatching authority`() {
    val creator = stubber.creators.default()
    val token = stubber.tokens(creator).real()

    assertThat {
      manager.requestSuperCreator(token)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("from super creator are allowed")
      }
  }

  @Test fun `requesting super creator fails with mismatching user ID`() {
    val creator = stubber.creators.default()
    val token = stubber.tokens(creator).real()

    assertThat {
      manager.requestSuperCreator(token)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("from super creator are allowed")
      }
  }

  @Test fun `requesting super creator succeeds with correct requester`() {
    val creator = stubber.creators.owner()
    val token = stubber.tokens(creator).real()

    assertThat(
      manager.requestSuperCreator(token),
    ).isDataClassEqualTo(creator)
  }

  // endregion

  // region Special access

  @Test fun `requesting project access fails with requesting peer SEARCH (setting is false)`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, anyoneCanSearch = false)
    val user = stubber.users(project).default()

    assertThat {
      manager.requestProjectAccess(
        authData = stubber.tokens(user).real(),
        targetId = project.id,
        privilege = USER_SEARCH,
      ).cleanStubArtifacts()
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Only ${USER_SEARCH.level.groupName} are authorized")
      }
  }

  @Test fun `requesting project access succeeds with requesting peer SEARCH (setting is true)`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, anyoneCanSearch = true)
    val user = stubber.users(project).default()

    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(user).real(),
        targetId = project.id,
        privilege = USER_SEARCH,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  @Test fun `requesting project access succeeds with requesting inferior SEARCH`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator)
    val user = stubber.users(project).admin()

    assertThat(
      manager.requestProjectAccess(
        authData = stubber.tokens(user).real(),
        targetId = project.id,
        privilege = USER_SEARCH,
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(project.cleanStubArtifacts())
  }

  @Test fun `requesting authority self-change fails if not owner`() {
    val project = stubber.projects.creator()
    val self = stubber.users(project).admin()
    val token = stubber.tokens(self).real()

    assertThat {
      manager.requestUserAccess(token, self.id, USER_WRITE_AUTHORITY)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Only ${OWNER.groupName} are authorized")
      }
  }

  @Test fun `requesting authority self-change succeeds if owner`() {
    val project = stubber.projects.creator()
    val self = stubber.users(project).owner()
    val token = stubber.tokens(self).real()

    assertThat(
      manager.requestUserAccess(token, self.id, USER_WRITE_AUTHORITY)
        .cleanStubArtifacts(),
    ).isDataClassEqualTo(self.cleanStubArtifacts())
  }

  // endregion

  // region Project State

  @Test fun `fetching project state fails with invalid project ID`() {
    assertThat {
      manager.fetchProjectState(-1)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `fetching project state, features resolve correctly`() {
    val project = stubber.projects.new(status = Status.SUSPENDED).cleanStubArtifacts()

    assertThat(
      manager.fetchProjectState(project.id)
        .cleanStubArtifacts(),
    )
      .isDataClassEqualTo(
        ProjectState(
          project = project,
          usableFeatures = listOf(Feature.BASIC, Feature.USERS),
          unusableFeatures = listOf(Feature.EMAILS, Feature.SMS, Feature.PUSH),
        ),
      )
  }

  @Test fun `requiring functional project fails on non-active project`() {
    val project = stubber.projects.new(status = Status.SUSPENDED)

    assertThat {
      manager.requireProjectFunctional(project.id)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project is set to")
      }
  }

  @Test fun `requiring functional project fails when 'on hold' is true`() {
    val project = stubber.projects.new()

    assertThat {
      manager.requireProjectFunctional(project.id)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("on hold")
      }
  }

  @Test fun `requiring functional project succeeds when configured`() {
    val project = stubber.projects.new(activateNow = true)

    assertThat {
      manager.requireProjectFunctional(project.id)
    }
      .isSuccess()
  }

  @Test fun `requiring functional features succeeds with configured features`() {
    val project = stubber.projects.new(activateNow = true)

    assertThat {
      manager.requireProjectFeaturesFunctional(project.id, Feature.BASIC, Feature.USERS)
    }
      .isSuccess()
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

  private fun Project.cleanStubArtifacts() = copy(
    createdAt = timeProvider.currentDate,
    updatedAt = timeProvider.currentDate,
  ).cleanDates()

  private fun Project.cleanDates() = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.DAYS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.DAYS),
  )

  private fun ProjectState.cleanStubArtifacts() = copy(
    project = project.cleanStubArtifacts(),
  )

  // endregion

}
