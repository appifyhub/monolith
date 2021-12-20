package com.appifyhub.monolith.repository.auth

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isSuccess
import assertk.assertions.isTrue
import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.auth.ops.TokenCreator
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.security.JwtClaims
import com.appifyhub.monolith.security.JwtHelper
import com.appifyhub.monolith.security.JwtHelper.Claims
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import java.time.Duration
import java.util.Date
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class AuthRepositoryImplTest {

  private val jwtHelper = mock<JwtHelper>()
  private val tokenDetailsRepo = mock<TokenDetailsRepository>()
  private val userRepo = mock<UserRepository>()
  private val timeProvider = TimeProviderFake()

  private val repository: AuthRepository = AuthRepositoryImpl(
    jwtHelper = jwtHelper,
    userRepository = userRepo,
    tokenDetailsRepository = tokenDetailsRepo,
    timeProvider = timeProvider,
  )

  @BeforeEach fun setup() {
    tokenDetailsRepo.stub {
      onGeneric { addToken(any()) } doAnswer { it.arguments.first() as TokenDetails }
    }
    userRepo.stub {
      onGeneric { fetchUserByUserId(any()) } doReturn Stubs.user
    }
    jwtHelper.stub {
      onGeneric { createJwtForClaims(any(), any(), any(), any()) } doReturn Stubs.tokenValue
      onGeneric { extractPropertiesFromJwt(any()) } doReturn Stubs.jwtClaims
    }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `create token succeeds with only mandatory properties`() {
    val createTime = DateTimeMapper.parseAsDateTime("2020-10-20 14:45")
    val expireTime = DateTimeMapper.parseAsDateTime("2020-10-21 14:45")
    timeProvider.staticTime = { createTime.time }

    val creator = TokenCreator(
      id = Stubs.userId,
      authority = Stubs.user.authority,
      origin = null,
      ipAddress = null,
      geo = null,
      isStatic = false,
    )

    assertThat(repository.createToken(creator))
      .isDataClassEqualTo(
        TokenDetails(
          tokenValue = Stubs.tokenValue,
          isBlocked = false,
          createdAt = createTime,
          expiresAt = expireTime,
          ownerId = Stubs.userId,
          authority = Stubs.user.authority,
          origin = null,
          ipAddress = null,
          geo = null,
          isStatic = false,
        )
      )

    verify(jwtHelper).createJwtForClaims(
      subject = creator.id.toUniversalFormat(),
      claims = mapOf(
        Claims.USER_ID to creator.id.userId,
        Claims.PROJECT_ID to creator.id.projectId,
        Claims.UNIVERSAL_ID to creator.id.toUniversalFormat(),
        Claims.AUTHORITIES to "DEFAULT,MODERATOR,ADMIN", // Stubs.user is ADMIN
        Claims.IS_STATIC to creator.isStatic,
      ),
      createdAt = createTime,
      expiresAt = expireTime,
    )
  }

  @Test fun `create token succeeds with all properties`() {
    val createTime = Stubs.tokenDetails.createdAt
    val expireTime = Date(createTime.time + TimeUnit.DAYS.toMillis(1))
    timeProvider.staticTime = { createTime.time }

    assertThat(repository.createToken(Stubs.tokenCreator))
      .isDataClassEqualTo(
        Stubs.tokenDetails.copy(
          expiresAt = expireTime,
          isBlocked = false,
        )
      )

    verify(jwtHelper).createJwtForClaims(
      subject = Stubs.universalUserId,
      claims = HashMap(Stubs.jwtClaims).apply {
        remove(Claims.VALUE)
        remove(Claims.CREATED_AT)
        remove(Claims.EXPIRES_AT)
      },
      createdAt = createTime,
      expiresAt = expireTime,
    )
  }

  @Test fun `create static token succeeds with only mandatory properties`() {
    val createTime = DateTimeMapper.parseAsDateTime("2020-10-20 14:45")
    val expireTime = DateTimeMapper.parseAsDateTime("2020-10-30 14:45")
    timeProvider.staticTime = { createTime.time }

    val creator = TokenCreator(
      id = Stubs.userId,
      authority = Stubs.user.authority,
      origin = null,
      ipAddress = null,
      geo = null,
      isStatic = true,
    )

    assertThat(repository.createToken(creator))
      .isDataClassEqualTo(
        TokenDetails(
          tokenValue = Stubs.tokenValue,
          isBlocked = false,
          createdAt = createTime,
          expiresAt = expireTime,
          ownerId = Stubs.userId,
          authority = Stubs.user.authority,
          origin = null,
          ipAddress = null,
          geo = null,
          isStatic = true,
        )
      )

    verify(jwtHelper).createJwtForClaims(
      subject = creator.id.toUniversalFormat(),
      claims = mapOf(
        Claims.USER_ID to creator.id.userId,
        Claims.PROJECT_ID to creator.id.projectId,
        Claims.UNIVERSAL_ID to creator.id.toUniversalFormat(),
        Claims.AUTHORITIES to "DEFAULT,MODERATOR,ADMIN", // Stubs.user is ADMIN
        Claims.IS_STATIC to creator.isStatic,
      ),
      createdAt = createTime,
      expiresAt = expireTime,
    )
  }

  @Test fun `create static token succeeds with all properties`() {
    val createTime = Stubs.tokenDetails.createdAt
    val expireTime = Date(createTime.time + TimeUnit.DAYS.toMillis(10))
    timeProvider.staticTime = { createTime.time }

    assertThat(repository.createToken(Stubs.tokenCreator.copy(isStatic = true)))
      .isDataClassEqualTo(
        Stubs.tokenDetails.copy(
          expiresAt = expireTime,
          isBlocked = false,
          isStatic = true,
        )
      )

    verify(jwtHelper).createJwtForClaims(
      subject = Stubs.universalUserId,
      claims = HashMap(Stubs.jwtClaims).apply {
        remove(Claims.VALUE)
        remove(Claims.CREATED_AT)
        remove(Claims.EXPIRES_AT)
        set(Claims.IS_STATIC, true)
      },
      createdAt = createTime,
      expiresAt = expireTime,
    )
  }

  @Test fun `check is valid is false when token is expired (shallow)`() {
    val jwt = newJwt()
    timeProvider.advanceBy(Duration.ofDays(2))

    assertThat(repository.isTokenValid(jwt, shallow = true))
      .isFalse()
  }

  @Test fun `check is valid is true when token is non-expired (shallow)`() {
    val jwt = newJwt()

    assertThat(repository.isTokenValid(jwt, shallow = true))
      .isTrue()
  }

  @Test fun `check is valid is false when token is blocked`() {
    tokenDetailsRepo.stub {
      onGeneric { checkIsBlocked(any()) } doReturn true
    }
    val jwt = newJwt()

    assertThat(repository.isTokenValid(jwt, shallow = false))
      .isFalse()
  }

  @Test fun `check is valid is false when token is expired`() {
    tokenDetailsRepo.stub {
      onGeneric { checkIsBlocked(any()) } doReturn false
      onGeneric { checkIsExpired(any()) } doReturn true
    }
    val jwt = newJwt()

    assertThat(repository.isTokenValid(jwt, shallow = false))
      .isFalse()
  }

  @Test fun `check is valid is true when token is valid`() {
    tokenDetailsRepo.stub {
      onGeneric { checkIsBlocked(any()) } doReturn false
      onGeneric { checkIsExpired(any()) } doReturn false
    }
    val jwt = newJwt()

    assertThat(repository.isTokenValid(jwt, shallow = false))
      .isTrue()
  }

  @Test fun `check is static is false when token is non-static`() {
    tokenDetailsRepo.stub {
      onGeneric { checkIsStatic(any()) } doReturn false
    }
    val jwt = newJwt()

    assertThat(repository.isTokenStatic(jwt))
      .isFalse()
  }

  @Test fun `check is static is true when token is static`() {
    tokenDetailsRepo.stub {
      onGeneric { checkIsStatic(any()) } doReturn true
    }
    val jwt = newJwt()

    assertThat(repository.isTokenStatic(jwt))
      .isTrue()
  }

  @Test fun `resolve shallow user succeeds with all properties`() {
    timeProvider.staticTime = { Stubs.tokenDetails.createdAt.time }

    assertThat(
      repository.resolveShallowUser(
        newJwt(
          tokenValue = Stubs.tokenValue,
          createdAt = Stubs.tokenDetails.createdAt,
          expiresAt = Stubs.tokenDetails.expiresAt,
          claims = Stubs.jwtClaims,
        )
      )
    ).isDataClassEqualTo(
      Stubs.user.copy(
        // lots of changes for shallow user...
        name = null,
        type = User.Type.PERSONAL,
        allowsSpam = false,
        contact = null,
        contactType = User.ContactType.CUSTOM,
        signature = "spring-asks-for-it",
        verificationToken = null,
        birthday = null,
        company = null,
        languageTag = null,
        createdAt = timeProvider.currentDate,
        updatedAt = timeProvider.currentDate,
      )
    )
  }

  @Test fun `fetch token details succeeds`() {
    tokenDetailsRepo.stub {
      onGeneric { fetchTokenDetails(any()) } doReturn Stubs.tokenDetails
    }

    assertThat(repository.fetchTokenDetails(newJwt()))
      .isDataClassEqualTo(Stubs.tokenDetails)
  }

  @Test fun `fetch all token details succeeds`() {
    val validTokens = listOf(Stubs.tokenDetails.copy(tokenValue = "v"))
    val blockedTokens = listOf(Stubs.tokenDetails.copy(tokenValue = "b"))
    val allTokens = validTokens + blockedTokens
    tokenDetailsRepo.stub {
      onGeneric { fetchAllValidTokens(any(), any()) } doReturn validTokens
      onGeneric { fetchAllBlockedTokens(any(), any()) } doReturn blockedTokens
      onGeneric { fetchAllTokens(any(), any()) } doReturn allTokens
    }

    assertAll {
      assertThat(repository.fetchAllTokenDetails(newJwt(), valid = null))
        .isEqualTo(allTokens)

      assertThat(repository.fetchAllTokenDetails(newJwt(), valid = true))
        .isEqualTo(validTokens)

      assertThat(repository.fetchAllTokenDetails(newJwt(), valid = false))
        .isEqualTo(blockedTokens)
    }
  }

  @Test fun `fetch all token details for another user succeeds`() {
    val validTokens = listOf(Stubs.tokenDetails.copy(tokenValue = "v"))
    val blockedTokens = listOf(Stubs.tokenDetails.copy(tokenValue = "b"))
    val allTokens = validTokens + blockedTokens
    tokenDetailsRepo.stub {
      onGeneric { fetchAllValidTokens(any(), any()) } doReturn validTokens
      onGeneric { fetchAllBlockedTokens(any(), any()) } doReturn blockedTokens
      onGeneric { fetchAllTokens(any(), any()) } doReturn allTokens
    }
    val randomUserId = UserId("userId", Stubs.project.id)

    assertAll {
      assertThat(repository.fetchAllTokenDetailsFor(id = randomUserId, valid = null))
        .isEqualTo(allTokens)

      assertThat(repository.fetchAllTokenDetailsFor(id = randomUserId, valid = true))
        .isEqualTo(validTokens)

      assertThat(repository.fetchAllTokenDetailsFor(id = randomUserId, valid = false))
        .isEqualTo(blockedTokens)
    }
  }

  @Test fun `unauthorize token succeeds`() {
    tokenDetailsRepo.stub {
      onGeneric { blockToken(any()) } doReturn Stubs.tokenDetails
    }

    assertThat { repository.unauthorizeToken(newJwt()) }
      .isSuccess()
  }

  @Test fun `unauthorize all tokens succeeds`() {
    tokenDetailsRepo.stub {
      onGeneric { fetchAllValidTokens(Stubs.user, project = null) } doReturn listOf(Stubs.tokenDetails)
    }

    assertThat { repository.unauthorizeAllTokens(newJwt()) }
      .isSuccess()
    verify(tokenDetailsRepo).blockAllTokens(listOf(Stubs.tokenDetails.tokenValue))
  }

  @Test fun `unauthorize all tokens for another user succeeds`() {
    tokenDetailsRepo.stub {
      onGeneric { fetchAllValidTokens(Stubs.user, project = null) } doReturn listOf(Stubs.tokenDetails)
    }

    assertThat { repository.unauthorizeAllTokensFor(Stubs.userId) }
      .isSuccess()
    verify(tokenDetailsRepo).blockAllTokens(listOf(Stubs.tokenDetails.tokenValue))
  }

  @Test fun `unauthorize all tokens using a list succeeds`() {
    tokenDetailsRepo.stub {
      onGeneric { blockAllTokens(any()) } doReturn listOf(Stubs.tokenDetails)
    }

    assertThat { repository.unauthorizeAllTokens(listOf(Stubs.tokenValue)) }
      .isSuccess()
  }

  // Helpers

  private fun newJwt(
    tokenValue: String = "tokenValue",
    createdAt: Date = timeProvider.currentDate,
    expiresAt: Date = Date(createdAt.time + TimeUnit.DAYS.toMillis(1)),
    claims: JwtClaims = HashMap(Stubs.jwtClaims).apply {
      put(Claims.VALUE, tokenValue)
      put(Claims.CREATED_AT, createdAt)
      put(Claims.EXPIRES_AT, expiresAt)
      put(JwtClaimNames.SUB, Stubs.userId.toUniversalFormat())
    },
  ): JwtAuthenticationToken {
    val jwt = Jwt(
      tokenValue,
      createdAt.toInstant(),
      expiresAt.toInstant(),
      mapOf("typ" to "JWT", "alg" to "RS256"), // headers
      claims,
    )
    val authorities = jwt.claims[Claims.AUTHORITIES].toString()
      .split(",")
      .map { User.Authority.find(it, User.Authority.DEFAULT) }
      .toSet() // to remove potential duplicates
      .sortedByDescending { it.ordinal }
    return JwtAuthenticationToken(jwt, authorities, jwt.subject)
  }

}
