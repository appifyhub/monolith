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
import com.appifyhub.monolith.controller.admin.AdminAuthController.Endpoints.ADMIN_API_KEY
import com.appifyhub.monolith.controller.admin.AdminAuthController.Endpoints.ADMIN_AUTH
import com.appifyhub.monolith.controller.admin.AdminAuthController.Endpoints.ANY_USER_AUTH
import com.appifyhub.monolith.controller.admin.AdminAuthController.Endpoints.ANY_USER_TOKENS
import com.appifyhub.monolith.domain.user.User.Authority.ADMIN
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.network.auth.AdminCredentialsRequest
import com.appifyhub.monolith.network.auth.ApiKeyRequest
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBlankRequest
import com.appifyhub.monolith.util.bearerBodyRequest
import com.appifyhub.monolith.util.blankUriVariables
import com.appifyhub.monolith.util.bodyRequest
import java.time.Duration
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

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
  classes = [TestAppifyHubApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Suppress("SpringJavaInjectionPointsAutowiringInspection") // some weird thing with restTemplate
class AdminAuthControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var stubber: Stubber

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
      universalId = stubber.creators.default().id.toUniversalFormat(),
      secret = "invalid",
      origin = Stubs.userCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ADMIN_AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `auth user succeeds with valid credentials`() {
    val credentials = AdminCredentialsRequest(
      universalId = stubber.creators.default().id.toUniversalFormat(),
      secret = Stubs.userCredentialsRequest.secret,
      origin = Stubs.userCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$ADMIN_AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.tokenValue }.isNotEmpty()
    }
  }

  @Test fun `create api key fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ADMIN_API_KEY",
        method = HttpMethod.POST,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `create api key succeeds with valid credentials`() {
    val ownerToken = stubber.creatorTokens().real(OWNER).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val keyData = ApiKeyRequest(origin = Stubs.userCredentialsRequest.origin)

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$ADMIN_API_KEY",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(keyData, ownerToken),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.tokenValue }.isNotEmpty()
    }
  }

  @Test fun `get any user tokens fails when unauthorized`() {
    val targetId = stubber.creators.default().id

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_USER_TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to targetId.projectId,
          "userId" to targetId.userId,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get any user tokens succeeds for self`() {
    val self = stubber.creators.default()
    val token = stubber.tokens(self).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertThat(
      restTemplate.exchange<List<TokenDetailsResponse>>(
        url = "$baseUrl$ANY_USER_TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to self.id.projectId,
          "userId" to self.id.userId,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(
        listOf(stubber.latestTokenOf(self).toNetwork())
      )
    }
  }

  @Test fun `get any user tokens succeeds for lower rank`() {
    val project = stubber.projects.new()
    val adminToken = stubber.tokens(project).real(ADMIN).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val target = stubber.users(project).default()
    val targetToken = stubber.tokens(target).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertThat(
      restTemplate.exchange<List<TokenDetailsResponse>>(
        url = "$baseUrl$ANY_USER_TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(adminToken),
        uriVariables = mapOf(
          "projectId" to target.id.projectId,
          "userId" to target.id.userId,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(
        listOf(targetToken).map { stubber.tokenDetailsOf(it).toNetwork() }
      )
    }
  }

  @Test fun `get any user tokens succeeds with static token`() {
    val project = stubber.projects.new()
    val target = stubber.users(project).owner(idSuffix = "_another")
    val staticToken = stubber.tokens(project).real(OWNER, isStatic = true).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val targetToken = stubber.tokens(target).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertThat(
      restTemplate.exchange<List<TokenDetailsResponse>>(
        url = "$baseUrl$ANY_USER_TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(staticToken),
        uriVariables = mapOf(
          "projectId" to target.id.projectId,
          "userId" to target.id.userId,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(
        listOf(targetToken).map { stubber.tokenDetailsOf(it).toNetwork() }
      )
    }
  }

  @Test fun `unauth any user tokens fails when unauthorized`() {
    val targetId = stubber.creators.default().id

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_USER_AUTH",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to targetId.projectId,
          "userId" to targetId.userId,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `unauth any user succeeds for self`() {
    val self = stubber.creators.default()
    val token1 = stubber.tokens(self).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val token2 = stubber.tokens(self).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$ANY_USER_AUTH",
          method = HttpMethod.DELETE,
          requestEntity = bearerBlankRequest(token1),
          uriVariables = mapOf(
            "projectId" to self.id.projectId,
            "userId" to self.id.userId,
          ),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(stubber.isAuthorized(token1)).isFalse()
      assertThat(stubber.isAuthorized(token2)).isFalse()
    }
  }

  @Test fun `unauth any user succeeds for lower rank`() {
    val project = stubber.projects.new()
    val target = stubber.users(project).default()
    val adminToken = stubber.tokens(project).real(ADMIN).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val targetToken1 = stubber.tokens(target).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val targetToken2 = stubber.tokens(target).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$ANY_USER_AUTH",
          method = HttpMethod.DELETE,
          requestEntity = bearerBlankRequest(adminToken),
          uriVariables = mapOf(
            "projectId" to target.id.projectId,
            "userId" to target.id.userId,
          ),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(stubber.isAuthorized(targetToken1)).isFalse()
      assertThat(stubber.isAuthorized(targetToken2)).isFalse()
      assertThat(stubber.isAuthorized(adminToken)).isTrue()
    }
  }

  @Test fun `unauth any user succeeds with static token`() {
    val project = stubber.projects.new()
    val target = stubber.users(project).owner(idSuffix = "_another")
    val staticToken = stubber.tokens(project).real(OWNER, isStatic = true).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val targetToken1 = stubber.tokens(target).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val targetToken2 = stubber.tokens(target).real().token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$ANY_USER_AUTH",
          method = HttpMethod.DELETE,
          requestEntity = bearerBlankRequest(staticToken),
          uriVariables = mapOf(
            "projectId" to target.id.projectId,
            "userId" to target.id.userId,
          ),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(stubber.isAuthorized(targetToken1)).isFalse()
      assertThat(stubber.isAuthorized(targetToken2)).isFalse()
      assertThat(stubber.isAuthorized(staticToken)).isTrue()
    }
  }

}
