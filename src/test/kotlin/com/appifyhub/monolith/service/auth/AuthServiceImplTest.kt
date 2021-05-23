package com.appifyhub.monolith.service.auth

import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isFalse
import assertk.assertions.isSuccess
import assertk.assertions.isTrue
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority.ADMIN
import com.appifyhub.monolith.domain.user.User.Authority.DEFAULT
import com.appifyhub.monolith.domain.user.User.Authority.MODERATOR
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.service.user.UserService.UserPrivilege
import com.appifyhub.monolith.util.AuthTestHelper
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.annotation.DirtiesContext.MethodMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthServiceImplTest {

  @Autowired lateinit var service: AuthService
  @Autowired lateinit var authHelper: AuthTestHelper
  @Autowired lateinit var timeProvider: TimeProviderFake

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  // region Auth Verifications

  @Test fun `is authorized is false for expired token with default authority`() {
    val token = authHelper.newStubJwt()
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat(
      service.hasSelfAuthority(
        authData = token,
        authority = DEFAULT,
        shallow = true,
      )
    ).isFalse()
  }

  @Test fun `is authorized is true for valid token with default authority`() {
    assertThat(
      service.hasSelfAuthority(
        authData = authHelper.newStubJwt(),
        authority = DEFAULT,
        shallow = true,
      )
    ).isTrue()
  }

  @Test fun `is authorized is false for expired token (shallow)`() {
    val token = authHelper.newStubJwt()
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat(
      service.hasSelfAuthority(
        authData = token,
        authority = DEFAULT,
        shallow = true,
      )
    ).isFalse()
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `is authorized is false for expired token (non-shallow)`() {
    val token = authHelper.newStubJwt()
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat(
      service.hasSelfAuthority(
        authData = token,
        authority = DEFAULT,
        shallow = false,
      )
    ).isFalse()
  }

  @Test fun `is authorized is false for valid token with low authority (shallow)`() {
    assertThat(
      service.hasSelfAuthority(
        authData = authHelper.newRealJwt(DEFAULT),
        authority = MODERATOR,
        shallow = true,
      )
    ).isFalse()
  }

  @Test fun `is authorized is false for valid token with low authority (non-shallow)`() {
    assertThat(
      service.hasSelfAuthority(
        authData = authHelper.newRealJwt(DEFAULT),
        authority = MODERATOR,
        shallow = false,
      )
    ).isFalse()
  }

  @Test fun `is authorized is false for invalid user token`() {
    assertThat(
      service.hasSelfAuthority(
        authData = authHelper.newStubJwt(),
        authority = MODERATOR,
        shallow = false, // shallow is already tested in expired test case
      )
    ).isFalse()
  }

  @Test fun `is authorized is true for valid token with admin authority (shallow)`() {
    assertThat(
      service.hasSelfAuthority(
        authData = authHelper.newRealJwt(ADMIN),
        authority = ADMIN,
        shallow = true,
      )
    ).isTrue()
  }

  @Test fun `is authorized is true for valid token with admin authority (non-shallow)`() {
    assertThat(
      service.hasSelfAuthority(
        authData = authHelper.newRealJwt(ADMIN),
        authority = ADMIN,
        shallow = true,
      )
    ).isTrue()
  }

  @Test fun `is project owner is false for expired token (shallow)`() {
    val token = authHelper.newRealJwt(OWNER)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat(
      service.isProjectOwner(
        authData = token,
        shallow = true,
      )
    ).isFalse()
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `is project owner is false for expired token (non-shallow)`() {
    val token = authHelper.newRealJwt(OWNER)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat(
      service.isProjectOwner(
        authData = token,
        shallow = false,
      )
    ).isFalse()
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `is project owner is false for non-owner (shallow)`() {
    assertThat(
      service.isProjectOwner(
        authData = authHelper.newRealJwt(DEFAULT),
        shallow = true,
      )
    ).isFalse()
  }

  @Test fun `is project owner is false for non-owner (non-shallow)`() {
    assertThat(
      service.isProjectOwner(
        authData = authHelper.newRealJwt(DEFAULT),
        shallow = true,
      )
    ).isFalse()
  }

  @Test fun `is project owner is true for owner (shallow)`() {
    assertThat(
      service.isProjectOwner(
        authData = authHelper.newRealJwt(OWNER),
        shallow = true,
      )
    ).isTrue()
  }

  @Test fun `is project owner is true for owner (non-shallow)`() {
    assertThat(
      service.isProjectOwner(
        authData = authHelper.newRealJwt(OWNER),
        shallow = false,
      )
    ).isTrue()
  }

  @Test fun `resolving shallow user from token works()`() {
    val modernTime = DateTimeMapper.parseAsDate("2021-02-03 04:05")
    timeProvider.staticTime = { modernTime.time }
    val token = authHelper.newRealJwt(DEFAULT)

    assertThat(
      service.resolveShallowSelf(token)
        .copy(ownedTokens = emptyList()) // doesn't matter for this test
    ).isDataClassEqualTo(
      // no rich data in shallow user
      authHelper.defaultUser.copy(
        name = null,
        signature = "spring-asks-for-it",
        allowsSpam = false,
        birthday = null,
        company = null,
        verificationToken = null,
        contactType = User.ContactType.CUSTOM,
        contact = null,
        ownedTokens = emptyList(),
        createdAt = modernTime,
        updatedAt = modernTime,
      )
    )
  }

  @Test fun `resolving shallow user with invalid universal ID fails (default authority)`() {
    val moderatorToken = authHelper.newRealJwt(MODERATOR)

    assertThat {
      service.resolveShallowUser(authData = moderatorToken, universalId = "invalid")
    }
      .isFailure()
      .all {
        hasClass(NumberFormatException::class)
      }
  }

  @Test fun `resolving shallow user with mismatched ID fails (default authority)`() {
    val moderatorToken = authHelper.newRealJwt(MODERATOR)
    val user = authHelper.defaultUser

    assertThat {
      service.resolveShallowUser(authData = moderatorToken, universalId = user.id.toUniversalFormat())
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID and auth data mismatch")
      }
  }

  @Test fun `resolving shallow user from token works (default authority)`() {
    val modernTime = DateTimeMapper.parseAsDate("2021-02-03 04:05")
    timeProvider.staticTime = { modernTime.time }
    val defaultUser = authHelper.defaultUser
    val token = authHelper.newRealJwt(DEFAULT)

    assertThat(
      service.resolveShallowUser(authData = token, universalId = defaultUser.id.toUniversalFormat())
        .copy(ownedTokens = emptyList()) // doesn't matter for this test
    ).isDataClassEqualTo(
      // no rich data in shallow user
      Stubs.user.copy(
        id = authHelper.defaultUser.id,
        name = null,
        signature = "spring-asks-for-it",
        type = User.Type.PERSONAL,
        authority = DEFAULT,
        contactType = User.ContactType.CUSTOM,
        contact = null,
        allowsSpam = false,
        birthday = null,
        verificationToken = null,
        company = null,
        ownedTokens = emptyList(),
        account = null,
        createdAt = modernTime,
        updatedAt = modernTime,
      )
    )
  }

  @Test fun `resolving shallow user from token works (owner authority)`() {
    val modernTime = DateTimeMapper.parseAsDate("2021-02-03 04:05")
    timeProvider.staticTime = { modernTime.time }
    val owner = authHelper.ownerUser
    val token = authHelper.newRealJwt(OWNER)

    assertThat(
      service.resolveShallowUser(authData = token, universalId = owner.id.toUniversalFormat())
        .copy(ownedTokens = emptyList()) // doesn't matter for this test
    ).isDataClassEqualTo(
      // no rich data in shallow user
      Stubs.user.copy(
        id = authHelper.ownerUser.id,
        name = null,
        signature = "spring-asks-for-it",
        type = User.Type.PERSONAL,
        authority = OWNER,
        contactType = User.ContactType.CUSTOM,
        contact = null,
        allowsSpam = false,
        birthday = null,
        verificationToken = null,
        company = null,
        ownedTokens = emptyList(),
        account = Account(
          id = authHelper.ownerUser.account!!.id,
          createdAt = modernTime,
          updatedAt = modernTime,
        ),
        createdAt = modernTime,
        updatedAt = modernTime,
      )
    )
  }

  @Test fun `requesting user access with invalid user ID fails`() {
    assertThat {
      service.requestAccessFor(
        authData = authHelper.newRealJwt(OWNER),
        targetId = UserId("", authHelper.adminProject.id),
        privilege = UserPrivilege.READ,
      )
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `requesting user access fails with expired token`() {
    val token = authHelper.newRealJwt(OWNER)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.requestAccessFor(
        authData = token,
        targetId = authHelper.ownerUser.id,
        privilege = UserPrivilege.READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `requesting user access fails with requesting READ for superiors`() {
    assertThat {
      service.requestAccessFor(
        authData = authHelper.newRealJwt(DEFAULT),
        targetId = authHelper.moderatorUser.id,
        privilege = UserPrivilege.READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only moderators are authorized")
      }
  }

  @Test fun `requesting user access fails with requesting WRITE for superiors`() {
    assertThat {
      service.requestAccessFor(
        authData = authHelper.newRealJwt(ADMIN),
        targetId = authHelper.ownerUser.id,
        privilege = UserPrivilege.WRITE,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only gods are authorized")
      }
  }

  @Test fun `requesting user access fails with mismatching projects`() {
    val anotherProject = authHelper.ensureProject(forceNewProject = true)
    assertThat {
      service.requestAccessFor(
        authData = authHelper.newRealJwt(ADMIN, isStatic = true),
        targetId = authHelper.ownerUser.id.copy(projectId = anotherProject.id),
        privilege = UserPrivilege.READ,
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Only requests within the same project are allowed")
      }
  }

  @Test fun `requesting user access succeeds with requesting READ for self`() {
    assertThat(
      service.requestAccessFor(
        authData = authHelper.newRealJwt(DEFAULT),
        targetId = authHelper.defaultUser.id,
        privilege = UserPrivilege.READ,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(authHelper.defaultUser.cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting WRITE for self`() {
    assertThat(
      service.requestAccessFor(
        authData = authHelper.newRealJwt(OWNER),
        targetId = authHelper.ownerUser.id,
        privilege = UserPrivilege.WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(authHelper.ownerUser.cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting READ for inferiors`() {
    assertThat(
      service.requestAccessFor(
        authData = authHelper.newRealJwt(MODERATOR),
        targetId = authHelper.defaultUser.id,
        privilege = UserPrivilege.READ,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(authHelper.defaultUser.cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting WRITE for inferiors`() {
    assertThat(
      service.requestAccessFor(
        authData = authHelper.newRealJwt(ADMIN),
        targetId = authHelper.moderatorUser.id,
        privilege = UserPrivilege.WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(authHelper.moderatorUser.cleanStubArtifacts())
  }

  @Test fun `requesting user access succeeds with requesting WRITE for another owner with static token`() {
    val anotherOwner = authHelper.ensureUser(OWNER, forceNewOwner = true)
    assertThat(
      service.requestAccessFor(
        authData = authHelper.newRealJwt(OWNER, isStatic = true),
        targetId = anotherOwner.id,
        privilege = UserPrivilege.WRITE,
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(anotherOwner.cleanStubArtifacts())
  }

  // endregion

  // region Auth Actions

  @Test fun `auth user fails with invalid universal ID`() {
    assertThat {
      service.resolveUser("invalid", "signature")
    }
      .isFailure()
      .hasClass(NumberFormatException::class)
  }

  @Test fun `auth user fails with invalid user ID`() {
    assertThat {
      service.resolveUser(UserId("\na b\t", -1).toUniversalFormat(), "signature")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `auth user fails with invalid signature`() {
    assertThat {
      service.resolveUser(authHelper.ensureUser(DEFAULT).id.toUniversalFormat(), "\n\t")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `auth user fails with wrong signature`() {
    assertThat {
      service.resolveUser(
        universalId = authHelper.ensureUser(DEFAULT).id.toUniversalFormat(),
        signature = "pass",
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Invalid credentials")
      }
  }

  @Test fun `auth user succeeds with correct signature`() {
    assertThat(
      service.resolveUser(
        universalId = authHelper.ensureUser(MODERATOR).id.toUniversalFormat(),
        signature = "password",
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(
      authHelper.moderatorUser.cleanStubArtifacts()
    )
  }

  @Test fun `auth admin fails with invalid universal ID`() {
    assertThat {
      service.resolveAdmin("invalid", "signature")
    }
      .isFailure()
      .hasClass(NumberFormatException::class)
  }

  @Test fun `auth admin fails with invalid user ID`() {
    assertThat {
      service.resolveAdmin(UserId("\na b\t", -1).toUniversalFormat(), "signature")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `auth admin fails with invalid signature`() {
    assertThat {
      service.resolveAdmin(authHelper.ensureUser(ADMIN).id.toUniversalFormat(), "\n\t")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `auth admin fails with wrong signature`() {
    assertThat {
      service.resolveAdmin(
        universalId = authHelper.ensureUser(ADMIN).id.toUniversalFormat(),
        signature = "pass",
      )
    }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Invalid credentials")
      }
  }

  @Test fun `auth admin succeeds with correct signature`() {
    assertThat(
      service.resolveUser(
        universalId = authHelper.ensureUser(ADMIN).id.toUniversalFormat(),
        signature = "password",
      ).cleanStubArtifacts()
    ).isDataClassEqualTo(
      authHelper.adminUser.cleanStubArtifacts()
    )
  }

  // endregion

  // region Tokens

  @Test fun `create token fails with invalid origin`() {
    assertThat {
      service.createTokenFor(authHelper.defaultUser, origin = "\n\t", ipAddress = null)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Origin")
      }
  }

  @Test fun `create token fails with invalid IP address`() {
    assertThat {
      service.createTokenFor(authHelper.defaultUser, origin = "origin", ipAddress = "abc")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("IP Address")
      }
  }

  @Test fun `create token succeeds with valid user data and non-empty optionals`() {
    assertThat(service.createTokenFor(authHelper.defaultUser, origin = "Some Origin", ipAddress = Stubs.ipAddress))
      .transform { it.split(".")[1] } // take the token content
      .transform { Base64.getDecoder().decode(it).toString(Charsets.UTF_8) } // convert to JSON
      .all {
        contains("Some Origin")
        contains(Stubs.ipAddress)
        contains(Stubs.geoMerged)
        contains(DEFAULT.name)
        contains(authHelper.defaultUser.id.toUniversalFormat())
      }
  }

  @Test fun `create token succeeds with valid user data and null optionals`() {
    assertThat(service.createTokenFor(authHelper.ownerUser, origin = null, ipAddress = null))
      .transform { it.split(".")[1] } // take the token content
      .transform { Base64.getDecoder().decode(it).toString(Charsets.UTF_8) } // convert to JSON
      .all {
        doesNotContain("origin", ignoreCase = true)
        doesNotContain("ip_address", ignoreCase = true)
        doesNotContain("geo", ignoreCase = true)
        contains(DEFAULT.name)
        contains(MODERATOR.name)
        contains(ADMIN.name)
        contains(OWNER.name)
        contains(authHelper.ownerUser.id.toUniversalFormat())
      }
  }

  @Test fun `create static token fails with invalid origin`() {
    assertThat {
      service.createStaticTokenFor(authHelper.ownerUser, origin = "\n\t", ipAddress = null)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Origin")
      }
  }

  @Test fun `create static token fails with invalid IP address`() {
    assertThat {
      service.createStaticTokenFor(authHelper.ownerUser, origin = "origin", ipAddress = "abc")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("IP Address")
      }
  }

  @Test fun `create static token fails with non-owner user`() {
    assertThat {
      service.createStaticTokenFor(authHelper.adminUser, origin = null, ipAddress = null)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Only ${OWNER.groupName} can create static tokens")
      }
  }

  @Test fun `create static token succeeds with valid user data and non-empty optionals`() {
    assertThat(service.createStaticTokenFor(authHelper.ownerUser, origin = "Some Origin", ipAddress = Stubs.ipAddress))
      .transform { it.split(".")[1] } // take the token content
      .transform { Base64.getDecoder().decode(it).toString(Charsets.UTF_8) } // convert to JSON
      .all {
        contains("Some Origin")
        contains(Stubs.ipAddress)
        contains(Stubs.geoMerged)
        contains(DEFAULT.name)
        contains(MODERATOR.name)
        contains(ADMIN.name)
        contains(OWNER.name)
        contains(authHelper.ownerUser.id.toUniversalFormat())
      }
  }

  @Test fun `create static token succeeds with valid user data and null optionals`() {
    assertThat(service.createStaticTokenFor(authHelper.ownerUser, origin = null, ipAddress = null))
      .transform { it.split(".")[1] } // take the token content
      .transform { Base64.getDecoder().decode(it).toString(Charsets.UTF_8) } // convert to JSON
      .all {
        doesNotContain("origin", ignoreCase = true)
        doesNotContain("ip_address", ignoreCase = true)
        doesNotContain("geo", ignoreCase = true)
        contains(DEFAULT.name)
        contains(MODERATOR.name)
        contains(ADMIN.name)
        contains(OWNER.name)
        contains(authHelper.ownerUser.id.toUniversalFormat())
      }
  }

  @Test fun `refresh token fails with invalid token`() {
    val token = authHelper.newStubJwt()

    assertThat {
      service.refreshAuth(token, ipAddress = null)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `refresh token fails with invalid IP address`() {
    val token = authHelper.newRealJwt(DEFAULT)

    assertThat {
      service.refreshAuth(token, ipAddress = "abc")
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("IP Address")
      }
  }

  @Test fun `refresh token fails with expired token`() {
    val token = authHelper.newRealJwt(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.refreshAuth(token, ipAddress = null)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `refresh token fails with static token`() {
    val token = authHelper.newRealJwt(OWNER, isStatic = true)

    assertThat {
      service.refreshAuth(token, ipAddress = null)
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Can't refresh static tokens")
      }
  }

  @Test fun `refresh token grants a new token with valid auth data`() {
    assertThat(service.refreshAuth(authHelper.newRealJwt(DEFAULT), ipAddress = "1.2.3.4"))
      .transform { it.split(".")[1] } // take the token content
      .transform { Base64.getDecoder().decode(it).toString(Charsets.UTF_8) } // convert to JSON
      .all {
        contains(Stubs.userCredentialsRequest.origin!!)
        contains("1.2.3.4")
        contains(DEFAULT.name)
        contains(authHelper.defaultUser.id.toUniversalFormat())
      }
  }

  @Test fun `fetching token details fails with expired token`() {
    val token = authHelper.newRealJwt(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.fetchTokenDetails(token)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Suppress("SpellCheckingInspection")
  @Test fun `fetching token details succeeds with valid data`() {
    val jwt = authHelper.newRealJwt(DEFAULT)
    val defaultUser = authHelper.defaultUser

    assertThat(
      service.fetchTokenDetails(jwt).cleanStubArtifacts()
    )
      .isDataClassEqualTo(
        TokenDetails(
          tokenValue = jwt.token.tokenValue,
          createdAt = timeProvider.currentDate,
          isBlocked = false,
          origin = Stubs.userCredentialsRequest.origin,
          expiresAt = Date(timeProvider.currentMillis + Duration.ofDays(1).toMillis()),
          ownerId = defaultUser.id,
          authority = defaultUser.authority,
          ipAddress = null,
          geo = null,
          accountId = null,
          isStatic = false,
        ).cleanStubArtifacts()
      )
  }

  @Test fun `fetching all token details fails with expired token`() {
    val token = authHelper.newRealJwt(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.fetchAllTokenDetails(token, valid = true)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Suppress("SpellCheckingInspection")
  @Test fun `fetching all token details succeeds with valid data`() {
    val jwt = authHelper.newRealJwt(DEFAULT)
    val defaultUser = authHelper.defaultUser

    assertThat(
      service.fetchAllTokenDetails(jwt, valid = null).map { it.cleanStubArtifacts() }
    )
      .isEqualTo(
        listOf(
          TokenDetails(
            tokenValue = jwt.token.tokenValue,
            createdAt = timeProvider.currentDate,
            isBlocked = false,
            origin = Stubs.userCredentialsRequest.origin,
            expiresAt = Date(timeProvider.currentMillis + Duration.ofDays(1).toMillis()),
            ownerId = defaultUser.id,
            authority = defaultUser.authority,
            ipAddress = null,
            geo = null,
            accountId = null,
            isStatic = false,
          ).cleanStubArtifacts(),
        )
      )
  }

  @Test fun `fetching all token details for others fails with expired token`() {
    val token = authHelper.newRealJwt(OWNER)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.fetchAllTokenDetailsFor(token, valid = true, targetId = authHelper.defaultUser.id)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `fetching all token details for others fails with invalid user ID`() {
    val token = authHelper.newRealJwt(OWNER)

    assertThat {
      service.fetchAllTokenDetailsFor(token, valid = true, targetId = UserId("\t\n", -1))
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Suppress("SpellCheckingInspection")
  @Test fun `fetching all token for others details succeeds with valid data`() {
    val defaultJwt = authHelper.newRealJwt(DEFAULT)
    val defaultUser = authHelper.defaultUser
    val ownerJwt = authHelper.newRealJwt(OWNER)

    assertThat(
      service.fetchAllTokenDetailsFor(ownerJwt, valid = true, targetId = authHelper.defaultUser.id)
        .map { it.cleanStubArtifacts() }
    )
      .isEqualTo(
        listOf(
          TokenDetails(
            tokenValue = defaultJwt.token.tokenValue,
            createdAt = timeProvider.currentDate,
            isBlocked = false,
            origin = Stubs.userCredentialsRequest.origin,
            expiresAt = Date(timeProvider.currentMillis + Duration.ofDays(1).toMillis()),
            ownerId = defaultUser.id,
            authority = defaultUser.authority,
            ipAddress = null,
            geo = null,
            accountId = null,
            isStatic = false,
          ).cleanStubArtifacts(),
        )
      )
  }

  // endregion

  // region Unauth

  @Test fun `unauthorize fails with expired token`() {
    val token = authHelper.newRealJwt(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.unauthorize(token)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `unauthorize succeeds with valid auth data`() {
    val token = authHelper.newRealJwt(DEFAULT)

    assertAll {
      // unauth with the token
      assertThat { service.unauthorize(token) }
        .isSuccess()

      // and then try to re-auth with it
      assertThat { service.refreshAuth(token, ipAddress = null) }
        .isFailure()
    }
  }

  @Test fun `unauthorize all fails with expired token`() {
    val token = authHelper.newRealJwt(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.unauthorizeAll(token)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `unauthorize all succeeds with valid auth data`() {
    val token1 = authHelper.newRealJwt(DEFAULT)
    val token2 = authHelper.newRealJwt(DEFAULT)

    assertAll {
      // unauth with the token
      assertThat { service.unauthorizeAll(token1) }
        .isSuccess()

      // and then try to re-auth
      assertThat { service.refreshAuth(token1, ipAddress = null) }
        .isFailure()
      assertThat { service.refreshAuth(token2, ipAddress = null) }
        .isFailure()
    }
  }

  @Test fun `unauthorize all for others fails with expired token`() {
    val token = authHelper.newRealJwt(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.unauthorizeAllFor(token, targetId = authHelper.defaultUser.id)
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `unauthorize all for others fails with invalid ID`() {
    val token = authHelper.newRealJwt(DEFAULT)

    assertThat {
      service.unauthorizeAllFor(token, targetId = UserId("\t\n", -1))
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `unauthorize all for others succeeds with valid auth data`() {
    val token1 = authHelper.newRealJwt(DEFAULT)
    val token2 = authHelper.newRealJwt(DEFAULT)
    val tokenAdmin = authHelper.newRealJwt(ADMIN)

    assertAll {
      // unauth with the token
      assertThat { service.unauthorizeAllFor(tokenAdmin, targetId = authHelper.defaultUser.id) }
        .isSuccess()

      // and then try to re-auth
      assertThat { service.refreshAuth(token1, ipAddress = null) }
        .isFailure()
      assertThat { service.refreshAuth(token2, ipAddress = null) }
        .isFailure()

      // admin token should still work
      assertThat(
        service.hasSelfAuthority(tokenAdmin, authority = DEFAULT, shallow = false)
      ).isTrue()
    }
  }

  @Test fun `unauthorize tokens fails with expired token`() {
    val token = authHelper.newRealJwt(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat {
      service.unauthorizeTokens(token, tokenValues = emptyList())
    }
      .isFailure()
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `unauthorize tokens fails with invalid token`() {
    val token = authHelper.newRealJwt(DEFAULT)

    assertThat {
      service.unauthorizeTokens(token, tokenValues = listOf("\t\n"))
    }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Token ID")
      }
  }

  @Test fun `unauthorize tokens succeeds with valid auth data`() {
    val token1 = authHelper.newRealJwt(DEFAULT)
    val token2 = authHelper.newRealJwt(DEFAULT)
    val tokensToUnauth = listOf(token1, token2)
      .map { service.fetchTokenDetails(it).tokenValue }

    assertAll {
      // unauth with the token
      assertThat { service.unauthorizeTokens(token1, tokenValues = tokensToUnauth) }
        .isSuccess()

      // and then try to re-auth
      assertThat { service.refreshAuth(token1, ipAddress = null) }
        .isFailure()
      assertThat { service.refreshAuth(token2, ipAddress = null) }
        .isFailure()
    }
  }

  // endregion

  // region Helpers

  // leftovers from hacking in auth utils need to be removed
  private fun User.cleanStubArtifacts() = copy(
    createdAt = timeProvider.currentDate,
    verificationToken = null,
  ).cleanDates()

  fun User.cleanDates() = copy(
    birthday = birthday?.truncateTo(ChronoUnit.DAYS),
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS),
  )

  // leftovers from hacking in auth utils need to be removed
  private fun TokenDetails.cleanStubArtifacts() = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    expiresAt = expiresAt.truncateTo(ChronoUnit.SECONDS),
  )

  // endregion

}
