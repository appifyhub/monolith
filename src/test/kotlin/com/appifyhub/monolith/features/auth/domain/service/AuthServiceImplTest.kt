package com.appifyhub.monolith.features.auth.domain.service

import assertk.all
import assertk.assertAll
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority.ADMIN
import com.appifyhub.monolith.domain.user.User.Authority.DEFAULT
import com.appifyhub.monolith.domain.user.User.Authority.MODERATOR
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.extension.truncateTo
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
import java.util.Base64
import java.util.Date

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class AuthServiceImplTest {

  @Autowired lateinit var service: AuthService
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var stubber: Stubber

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  // region Auth Verifications

  @Test fun `requiring JWT succeeds`() {
    // just the basic happy case, all others are tested within the other tests
    val authData = stubber.creatorTokens().real(DEFAULT)
    assertThat(
      service.requireValidJwt(
        authData = authData,
        shallow = false,
      ),
    ).isEqualTo(authData)
  }

  @Test fun `resolving shallow user from token works`() {
    val modernTime = DateTimeMapper.parseAsDate("2021-02-03 04:05")
    timeProvider.staticTime = { modernTime.time }
    val token = stubber.creatorTokens().real(DEFAULT)

    assertThat(
      service.resolveShallowSelf(token),
    ).isDataClassEqualTo(
      // no rich data in shallow user
      stubber.creators.default().copy(
        name = null,
        signature = "spring-asks-for-it",
        allowsSpam = false,
        verificationToken = null,
        contactType = User.ContactType.CUSTOM,
        contact = null,
        birthday = null,
        company = null,
        languageTag = null,
        createdAt = modernTime,
        updatedAt = modernTime,
      ),
    )
  }

  @Test fun `resolving shallow user with invalid universal ID fails (default authority)`() {
    val moderatorToken = stubber.creatorTokens().real(MODERATOR)

    assertFailure {
      service.resolveShallowUser(authData = moderatorToken, universalId = "invalid")
    }
      .all {
        hasClass(NumberFormatException::class)
      }
  }

  @Test fun `resolving shallow user with mismatched ID fails (default authority)`() {
    val moderatorToken = stubber.creatorTokens().real(MODERATOR)
    val targetId = stubber.creators.default().id.toUniversalFormat()

    assertFailure {
      service.resolveShallowUser(authData = moderatorToken, universalId = targetId)
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID and auth data mismatch")
      }
  }

  @Test fun `resolving shallow user from token works (default authority)`() {
    val modernTime = DateTimeMapper.parseAsDate("2021-02-03 04:05")
    timeProvider.staticTime = { modernTime.time }
    val defaultUser = stubber.creators.default()
    val token = stubber.creatorTokens().real(DEFAULT)

    assertThat(
      service.resolveShallowUser(authData = token, universalId = defaultUser.id.toUniversalFormat()),
    ).isDataClassEqualTo(
      // no rich data in shallow user
      Stubs.user.copy(
        id = stubber.creators.default().id,
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
        languageTag = null,
        createdAt = modernTime,
        updatedAt = modernTime,
      ),
    )
  }

  @Test fun `resolving shallow user from token works (owner authority)`() {
    val modernTime = DateTimeMapper.parseAsDate("2021-02-03 04:05")
    timeProvider.staticTime = { modernTime.time }
    val owner = stubber.creators.owner()
    val token = stubber.tokens(owner).real()

    assertThat(
      service.resolveShallowUser(authData = token, universalId = owner.id.toUniversalFormat()),
    ).isDataClassEqualTo(
      // no rich data in shallow user
      Stubs.user.copy(
        id = stubber.creators.owner().id,
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
        languageTag = null,
        createdAt = modernTime,
        updatedAt = modernTime,
      ),
    )
  }

  // endregion

  // region Auth Actions

  @Test fun `auth user fails with invalid universal ID`() {
    assertFailure {
      service.resolveUser("invalid", "signature")
    }
      .hasClass(NumberFormatException::class)
  }

  @Test fun `auth user fails with invalid user ID`() {
    assertFailure {
      service.resolveUser(UserId("\na b\t", -1).toUniversalFormat(), "signature")
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `auth user fails with invalid signature`() {
    assertFailure {
      service.resolveUser(stubber.creators.default().id.toUniversalFormat(), "\n\t")
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `auth user fails with wrong signature`() {
    assertFailure {
      service.resolveUser(
        universalId = stubber.creators.default().id.toUniversalFormat(),
        signature = "pass",
      )
    }
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Invalid credentials")
      }
  }

  @Test fun `auth user fails with unverified user`() {
    val user = stubber.creators.default(autoVerified = false)

    assertFailure {
      service.resolveUser(
        universalId = user.id.toUniversalFormat(),
        signature = "password",
      )
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("not verified")
      }
  }

  @Test fun `auth user succeeds with correct signature`() {
    assertThat(
      service.resolveUser(
        universalId = stubber.creators.default().id.toUniversalFormat(),
        signature = "password",
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(
      stubber.creators.default().cleanStubArtifacts(),
    )
  }

  @Test fun `auth creator fails with invalid universal ID`() {
    assertFailure {
      service.resolveCreator("invalid", "signature")
    }
      .hasClass(NumberFormatException::class)
  }

  @Test fun `auth creator fails with invalid user ID`() {
    assertFailure {
      service.resolveCreator(UserId("\na b\t", -1).toUniversalFormat(), "signature")
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `auth creator fails with invalid signature`() {
    assertFailure {
      service.resolveCreator(stubber.creators.owner().id.toUniversalFormat(), "\n\t")
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `auth creator fails with wrong signature`() {
    assertFailure {
      service.resolveCreator(
        universalId = stubber.creators.owner().id.toUniversalFormat(),
        signature = "pass",
      )
    }
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Invalid credentials")
      }
  }

  @Test fun `auth creator fails with unverified user`() {
    val creator = stubber.creators.default(autoVerified = false)

    assertFailure {
      service.resolveUser(
        universalId = creator.id.toUniversalFormat(),
        signature = "password",
      )
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("not verified")
      }
  }

  @Test fun `auth creator succeeds with correct signature`() {
    assertThat(
      service.resolveUser(
        universalId = stubber.creators.default().id.toUniversalFormat(),
        signature = "password",
      ).cleanStubArtifacts(),
    ).isDataClassEqualTo(
      stubber.creators.default().cleanStubArtifacts(),
    )
  }

  // endregion

  // region Tokens

  @Test fun `create token fails with invalid origin`() {
    assertFailure {
      service.createTokenFor(stubber.creators.default(), origin = "\n\t", ipAddress = null)
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Origin")
      }
  }

  @Test fun `create token fails with invalid IP address`() {
    assertFailure {
      service.createTokenFor(stubber.creators.default(), origin = "origin", ipAddress = "abc")
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("IP Address")
      }
  }

  @Test fun `create token succeeds with valid user data and non-empty optionals`() {
    assertThat(service.createTokenFor(stubber.creators.default(), origin = "Some Origin", ipAddress = Stubs.ipAddress))
      .transform { it.split(".")[1] } // take the token content
      .transform { Base64.getDecoder().decode(it).toString(Charsets.UTF_8) } // convert to JSON
      .all {
        contains("Some Origin")
        contains(Stubs.ipAddress)
        contains(Stubs.geoMerged)
        contains(DEFAULT.name)
        contains(stubber.creators.default().id.toUniversalFormat())
      }
  }

  @Test fun `create token succeeds with valid user data and null optionals`() {
    assertThat(service.createTokenFor(stubber.creators.owner(), origin = null, ipAddress = null))
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
        contains(stubber.creators.owner().id.toUniversalFormat())
      }
  }

  @Test fun `create static token fails with invalid origin`() {
    assertFailure {
      service.createStaticTokenFor(stubber.creators.owner(), origin = "\n\t", ipAddress = null)
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Origin")
      }
  }

  @Test fun `create static token fails with invalid IP address`() {
    assertFailure {
      service.createStaticTokenFor(stubber.creators.owner(), origin = "origin", ipAddress = "abc")
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("IP Address")
      }
  }

  @Test fun `create static token fails with non-owner user`() {
    val project = stubber.projects.new()
    val adminUser = stubber.users(project).admin()
    assertFailure {
      service.createStaticTokenFor(adminUser, origin = null, ipAddress = null)
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Only ${OWNER.groupName} can create static tokens")
      }
  }

  @Test fun `create static token succeeds with valid (user) data and non-empty optionals`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).owner()
    assertThat(
      service.createStaticTokenFor(
        user = owner,
        origin = "Some Origin",
        ipAddress = Stubs.ipAddress,
      ),
    )
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
        contains(owner.id.toUniversalFormat())
      }
  }

  @Test fun `create static token succeeds with valid (user) data and null optionals`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).owner()
    assertThat(service.createStaticTokenFor(owner, origin = null, ipAddress = null))
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
        contains(owner.id.toUniversalFormat())
      }
  }

  @Test fun `create static token succeeds with valid (creator) data and null optionals`() {
    val user = stubber.creators.default()
    assertThat(service.createStaticTokenFor(user, origin = null, ipAddress = null))
      .transform { it.split(".")[1] } // take the token content
      .transform { Base64.getDecoder().decode(it).toString(Charsets.UTF_8) } // convert to JSON
      .all {
        doesNotContain("origin", ignoreCase = true)
        doesNotContain("ip_address", ignoreCase = true)
        doesNotContain("geo", ignoreCase = true)
        contains(DEFAULT.name)
        contains(user.id.toUniversalFormat())
      }
  }

  @Test fun `refresh token fails with invalid token`() {
    val token = stubber.creatorTokens().stub()

    assertFailure {
      service.refreshAuth(token, ipAddress = null)
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `refresh token fails with invalid IP address`() {
    val token = stubber.creatorTokens().real(DEFAULT)

    assertFailure {
      service.refreshAuth(token, ipAddress = "abc")
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("IP Address")
      }
  }

  @Test fun `refresh token fails with expired token`() {
    val token = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertFailure {
      service.refreshAuth(token, ipAddress = null)
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `refresh token fails with static token`() {
    val token = stubber.creatorTokens().real(OWNER, isStatic = true)

    assertFailure {
      service.refreshAuth(token, ipAddress = null)
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Can't refresh static tokens")
      }
  }

  @Test fun `refresh token grants a new token with valid auth data`() {
    assertThat(service.refreshAuth(stubber.creatorTokens().real(DEFAULT), ipAddress = "1.2.3.4"))
      .transform { it.split(".")[1] } // take the token content
      .transform { Base64.getDecoder().decode(it).toString(Charsets.UTF_8) } // convert to JSON
      .all {
        contains(Stubs.userCredentialsRequest.origin!!)
        contains("1.2.3.4")
        contains(DEFAULT.name)
        contains(stubber.creators.default().id.toUniversalFormat())
      }
  }

  @Test fun `fetching token details fails with expired token`() {
    val token = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertFailure {
      service.fetchTokenDetails(token)
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `fetching token details succeeds with valid data`() {
    val jwt = stubber.creatorTokens().real(DEFAULT)
    val defaultUser = stubber.creators.default()

    assertThat(
      service.fetchTokenDetails(jwt).cleanStubArtifacts(),
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
          isStatic = false,
        ).cleanStubArtifacts(),
      )
  }

  @Test fun `fetching all token details fails with expired token`() {
    val token = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertFailure {
      service.fetchAllTokenDetails(token, valid = true)
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `fetching all token details succeeds with valid data`() {
    val jwt = stubber.creatorTokens().real(DEFAULT)
    val defaultUser = stubber.creators.default()

    assertThat(
      service.fetchAllTokenDetails(jwt, valid = null).map { it.cleanStubArtifacts() },
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
            isStatic = false,
          ).cleanStubArtifacts(),
        ),
      )
  }

  @Test fun `fetching all token details for others fails with expired token`() {
    val owner = stubber.creators.owner()
    val token = stubber.tokens(owner).real()
    timeProvider.advanceBy(Duration.ofDays(2))

    assertFailure {
      service.fetchAllTokenDetailsFor(token, valid = true, targetId = stubber.creators.default().id)
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `fetching all token details for others fails with invalid user ID`() {
    val owner = stubber.creators.owner()
    val token = stubber.tokens(owner).real()

    assertFailure {
      service.fetchAllTokenDetailsFor(token, valid = true, targetId = UserId("\t\n", -1))
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `fetching all token for others details succeeds with valid data`() {
    val defaultJwt = stubber.creatorTokens().real(DEFAULT)
    val defaultUser = stubber.creators.default()
    val owner = stubber.creators.owner()
    val ownerJwt = stubber.tokens(owner).real()

    assertThat(
      service.fetchAllTokenDetailsFor(ownerJwt, valid = true, targetId = defaultUser.id)
        .map { it.cleanStubArtifacts() },
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
            isStatic = false,
          ).cleanStubArtifacts(),
        ),
      )
  }

  // endregion

  // region Unauth

  @Test fun `unauthorize fails with expired token`() {
    val token = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertFailure {
      service.unauthorize(token)
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `unauthorize succeeds with valid auth data`() {
    val token = stubber.creatorTokens().real(DEFAULT)

    assertAll {
      // unauth with the token
      assertThat(service.unauthorize(token))
        .isEqualTo(Unit)

      // and then try to re-auth with it
      assertFailure { service.refreshAuth(token, ipAddress = null) }
    }
  }

  @Test fun `unauthorize all fails with expired token`() {
    val token = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertFailure {
      service.unauthorizeAll(token)
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `unauthorize all succeeds with valid auth data`() {
    val token1 = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofHours(1))
    val token2 = stubber.creatorTokens().real(DEFAULT)

    assertAll {
      // unauth with the token
      assertThat(service.unauthorizeAll(token1))
        .isEqualTo(Unit)

      // and then try to re-auth
      assertFailure { service.refreshAuth(token1, ipAddress = null) }
      assertFailure { service.refreshAuth(token2, ipAddress = null) }
    }
  }

  @Test fun `unauthorize all for others fails with expired token`() {
    val token = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertFailure {
      service.unauthorizeAllFor(token, targetId = stubber.creators.default().id)
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `unauthorize all for others fails with invalid ID`() {
    val token = stubber.creatorTokens().real(DEFAULT)

    assertFailure {
      service.unauthorizeAllFor(token, targetId = UserId("\t\n", -1))
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `unauthorize all for others succeeds with valid auth data`() {
    val token1 = stubber.creatorTokens().real(DEFAULT)
    val token2 = stubber.creatorTokens().real(DEFAULT)
    val owner = stubber.creators.owner()
    val ownerToken = stubber.tokens(owner).real()

    assertAll {
      // unauth with the token
      assertThat(service.unauthorizeAllFor(ownerToken, targetId = stubber.creators.default().id))
        .isEqualTo(Unit)

      // and then try to re-auth
      assertFailure { service.refreshAuth(token1, ipAddress = null) }
      assertFailure { service.refreshAuth(token2, ipAddress = null) }
    }
  }

  @Test fun `unauthorize all by user ID fails with invalid ID`() {
    assertFailure {
      service.unauthorizeAllFor(UserId("\t\n", -1))
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `unauthorize all by user ID succeeds with valid auth data`() {
    val user = stubber.creators.default()
    val token1 = stubber.tokens(user).real()
    val token2 = stubber.tokens(user).real()

    assertAll {
      // unauth first
      assertThat(service.unauthorizeAllFor(stubber.creators.default().id))
        .isEqualTo(Unit)

      // and then try to re-auth
      assertFailure { service.refreshAuth(token1, ipAddress = null) }
      assertFailure { service.refreshAuth(token2, ipAddress = null) }
    }
  }

  @Test fun `unauthorize tokens fails with expired token`() {
    val token = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofDays(2))

    assertFailure {
      service.unauthorizeTokens(token, tokenValues = emptyList())
    }
      .all {
        hasClass(IllegalAccessException::class)
        messageContains("Invalid token for")
      }
  }

  @Test fun `unauthorize tokens fails with invalid token`() {
    val token = stubber.creatorTokens().real(DEFAULT)

    assertFailure {
      service.unauthorizeTokens(token, tokenValues = listOf("\t\n"))
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Token ID")
      }
  }

  @Test fun `unauthorize tokens succeeds with valid auth data`() {
    val token1 = stubber.creatorTokens().real(DEFAULT)
    timeProvider.advanceBy(Duration.ofHours(1))
    val token2 = stubber.creatorTokens().real(DEFAULT)
    val tokensToUnauth = listOf(token1, token2)
      .map { service.fetchTokenDetails(it).tokenValue }

    assertAll {
      // unauth with the token
      assertThat(service.unauthorizeTokens(token1, tokenValues = tokensToUnauth))
        .isEqualTo(Unit)

      // and then try to re-auth
      assertFailure { service.refreshAuth(token1, ipAddress = null) }
      assertFailure { service.refreshAuth(token2, ipAddress = null) }
    }
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

  // leftovers from hacking in auth utils need to be removed
  private fun TokenDetails.cleanStubArtifacts() = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    expiresAt = expiresAt.truncateTo(ChronoUnit.SECONDS),
  )

  // endregion

}
