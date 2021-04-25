package com.appifyhub.monolith.controller.admin

import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.admin.AdminAuthController.Endpoints.ADMIN_AUTH
import com.appifyhub.monolith.controller.admin.AdminAuthController.Endpoints.ANY_USER_AUTH
import com.appifyhub.monolith.controller.admin.AdminAuthController.Endpoints.ANY_USER_TOKENS
import com.appifyhub.monolith.domain.user.User.Authority.ADMIN
import com.appifyhub.monolith.domain.user.User.Authority.DEFAULT
import com.appifyhub.monolith.network.auth.AdminCredentialsRequest
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
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
class AdminAuthControllerTest {

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

  @Test fun `auth admin fails with invalid credentials`() {
    val credentials = AdminCredentialsRequest(
      universalId = authTestHelper.defaultUser.userId.toUniversalFormat(),
      secret = "invalid",
      origin = Stubs.userCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ADMIN_AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `auth user succeeds with valid credentials`() {
    val credentials = AdminCredentialsRequest(
      universalId = authTestHelper.adminUser.userId.toUniversalFormat(),
      secret = Stubs.userCredentialsRequest.secret,
      origin = Stubs.userCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$ADMIN_AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = emptyUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.token }.isNotEmpty()
    }
  }

  @Test fun `get any user tokens fails when unauthorized`() {
    val userId = authTestHelper.adminUser.userId

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_USER_TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to userId.projectId,
          "id" to userId.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get any user tokens succeeds for self`() {
    val user = authTestHelper.adminUser
    val token = authTestHelper.newRealToken(ADMIN)
    val expirationTime = Date(
      timeProvider.currentMillis +
        Duration.of(authTestHelper.expirationDaysDelta.toLong(), ChronoUnit.DAYS)
          .toMillis()
    )

    assertThat(
      restTemplate.exchange<List<TokenDetailsResponse>>(
        url = "$baseUrl$ANY_USER_TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token.token.tokenValue),
        uriVariables = mapOf(
          "projectId" to user.userId.projectId,
          "id" to user.userId.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(listOf(
        Stubs.tokenDetailsResponse.copy(
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          expiresAt = DateTimeMapper.formatAsDateTime(expirationTime),
          isBlocked = false,
          ownerId = user.userId.id,
          ownerUniversalId = user.userId.toUniversalFormat(),
          tokenId = authTestHelper.fetchLastTokenOf(user).token.tokenLocator,
        )
      ))
    }
  }

  @Test fun `get any user tokens succeeds for lower rank`() {
    authTestHelper.newRealToken(DEFAULT)
    val user = authTestHelper.defaultUser
    val token = authTestHelper.newRealToken(ADMIN)
    val expirationTime = Date(
      timeProvider.currentMillis +
        Duration.of(authTestHelper.expirationDaysDelta.toLong(), ChronoUnit.DAYS)
          .toMillis()
    )

    assertThat(
      restTemplate.exchange<List<TokenDetailsResponse>>(
        url = "$baseUrl$ANY_USER_TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token.token.tokenValue),
        uriVariables = mapOf(
          "projectId" to user.userId.projectId,
          "id" to user.userId.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(listOf(
        Stubs.tokenDetailsResponse.copy(
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          expiresAt = DateTimeMapper.formatAsDateTime(expirationTime),
          isBlocked = false,
          ownerId = user.userId.id,
          ownerUniversalId = user.userId.toUniversalFormat(),
          tokenId = authTestHelper.fetchLastTokenOf(user).token.tokenLocator,
        )
      ))
    }
  }

  @Test fun `unauth any user tokens fails when unauthorized`() {
    val userId = authTestHelper.adminUser.userId

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_USER_AUTH",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to userId.projectId,
          "id" to userId.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `unauth any user succeeds for self`() {
    val user = authTestHelper.adminUser
    val token1 = authTestHelper.newRealToken(ADMIN)
    val token2 = authTestHelper.newRealToken(ADMIN)

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$ANY_USER_AUTH",
          method = HttpMethod.DELETE,
          requestEntity = bearerEmptyRequest(token1.token.tokenValue),
          uriVariables = mapOf(
            "projectId" to user.userId.projectId,
            "id" to user.userId.id,
          ),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(authTestHelper.isAuthorized(token1)).isFalse()
      assertThat(authTestHelper.isAuthorized(token2)).isFalse()
    }
  }

  @Test fun `unauth any user succeeds for lower rank`() {
    val user = authTestHelper.defaultUser
    val adminToken = authTestHelper.newRealToken(ADMIN)
    val token1 = authTestHelper.newRealToken(DEFAULT)
    val token2 = authTestHelper.newRealToken(DEFAULT)

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$ANY_USER_AUTH",
          method = HttpMethod.DELETE,
          requestEntity = bearerEmptyRequest(adminToken.token.tokenValue),
          uriVariables = mapOf(
            "projectId" to user.userId.projectId,
            "id" to user.userId.id,
          ),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(authTestHelper.isAuthorized(token1)).isFalse()
      assertThat(authTestHelper.isAuthorized(token2)).isFalse()
      assertThat(authTestHelper.isAuthorized(adminToken)).isTrue()
    }
  }

}
