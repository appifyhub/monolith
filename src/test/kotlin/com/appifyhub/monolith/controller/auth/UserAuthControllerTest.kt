package com.appifyhub.monolith.controller.auth

import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.auth.UserAuthController.Endpoints.AUTH
import com.appifyhub.monolith.controller.auth.UserAuthController.Endpoints.TOKENS
import com.appifyhub.monolith.domain.user.User.Authority.DEFAULT
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.auth.UserCredentialsRequest
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.util.AuthTestHelper
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerEmptyRequest
import com.appifyhub.monolith.util.bodyRequest
import com.appifyhub.monolith.util.emptyUriVariables
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Date

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
  classes = [TestAppifyHubApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class UserAuthControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var authTestHelper: AuthTestHelper

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `auth user fails with invalid credentials`() {
    val credentials = UserCredentialsRequest(
      universalId = authTestHelper.defaultUser.userId.toUniversalFormat(),
      secret = "invalid",
      origin = Stubs.userCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `auth user succeeds with valid credentials`() {
    val credentials = UserCredentialsRequest(
      universalId = authTestHelper.defaultUser.userId.toUniversalFormat(),
      secret = Stubs.userCredentialsRequest.secret,
      origin = Stubs.userCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.token }.isNotEmpty()
    }
  }

  @Test fun `get current token fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get token succeeds with valid authorization`() {
    val user = authTestHelper.defaultUser
    val token = authTestHelper.newRealToken(DEFAULT).token.tokenValue
    val tokenId = authTestHelper.fetchLastTokenOf(user).token.tokenLocator
    val expirationTime = Date(
      timeProvider.currentMillis +
        Duration.of(authTestHelper.expirationDaysDelta.toLong(), ChronoUnit.DAYS)
          .toMillis()
    )

    assertThat(
      restTemplate.exchange<TokenDetailsResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.tokenDetailsResponse.copy(
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          expiresAt = DateTimeMapper.formatAsDateTime(expirationTime),
          isBlocked = false,
          ownerId = user.userId.id,
          ownerUniversalId = user.userId.toUniversalFormat(),
          tokenId = tokenId,
        )
      )
    }
  }

  @Test fun `get all tokens fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get all tokens succeeds with valid authorization`() {
    val user = authTestHelper.defaultUser
    authTestHelper.newRealToken(DEFAULT)
    val token = authTestHelper.newRealToken(DEFAULT).token.tokenValue
    val expirationTime = Date(
      timeProvider.currentMillis +
        Duration.of(authTestHelper.expirationDaysDelta.toLong(), ChronoUnit.DAYS)
          .toMillis()
    )
    val expectedTokens = authTestHelper.fetchAllTokensOf(user).map {
      Stubs.tokenDetailsResponse.copy(
        createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        expiresAt = DateTimeMapper.formatAsDateTime(expirationTime),
        isBlocked = false,
        ownerId = user.userId.id,
        ownerUniversalId = user.userId.toUniversalFormat(),
        tokenId = it.token.tokenLocator,
      )
    }

    assertThat(
      restTemplate.exchange<List<TokenDetailsResponse>>(
        url = "$baseUrl$TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(expectedTokens)
    }
  }

  @Test fun `refresh user fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.PUT,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `refresh user succeeds with valid authorization`() {
    val token = authTestHelper.newRealToken(DEFAULT).token.tokenValue

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.PUT,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.token }.all {
        isNotEmpty()
        isNotEqualTo(token)
      }
    }
  }

  @Test fun `unauth user fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$AUTH?all={all}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("all" to null),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `unauth user (single token) succeeds with valid authorization`() {
    val token = authTestHelper.newRealToken(DEFAULT)
    val tokenValue = token.token.tokenValue

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$AUTH?all={all}",
          method = HttpMethod.DELETE,
          requestEntity = bearerEmptyRequest(tokenValue),
          uriVariables = mapOf("all" to false),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(authTestHelper.isAuthorized(token)).isFalse()
    }
  }

  @Test fun `unauth user (all tokens) succeeds with valid authorization`() {
    val token1 = authTestHelper.newRealToken(DEFAULT)
    val token2 = authTestHelper.newRealToken(DEFAULT)
    val tokenValue = token1.token.tokenValue

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$AUTH?all={all}",
          method = HttpMethod.DELETE,
          requestEntity = bearerEmptyRequest(tokenValue),
          uriVariables = mapOf("all" to true),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(authTestHelper.isAuthorized(token1)).isFalse()
      assertThat(authTestHelper.isAuthorized(token2)).isFalse()
    }
  }

  @Test fun `unauth tokens fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$TOKENS?tokenIds={token1}&tokenIds={token2}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "token1" to null,
          "token2" to null,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `unauth tokens succeeds with valid authorization`() {
    val token1 = authTestHelper.newRealToken(DEFAULT)
    val tokenId1 = authTestHelper.fetchOwnedTokenFrom(token1).token.tokenLocator
    val token2 = authTestHelper.newRealToken(DEFAULT)
    val tokenId2 = authTestHelper.fetchOwnedTokenFrom(token2).token.tokenLocator
    val token3 = authTestHelper.newRealToken(DEFAULT)
    val tokenValue = token3.token.tokenValue

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$TOKENS?tokenIds={token1}&tokenIds={token2}",
          method = HttpMethod.DELETE,
          requestEntity = bearerEmptyRequest(tokenValue),
          uriVariables = mapOf(
            "token1" to tokenId1,
            "token2" to tokenId2,
          ),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(authTestHelper.isAuthorized(token1)).isFalse()
      assertThat(authTestHelper.isAuthorized(token2)).isFalse()
      assertThat(authTestHelper.isAuthorized(token3)).isTrue()
    }
  }

}