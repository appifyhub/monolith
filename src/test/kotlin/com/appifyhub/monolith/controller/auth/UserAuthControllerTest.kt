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
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBlankRequest
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
class UserAuthControllerTest {

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

  @Test fun `auth user fails with invalid credentials`() {
    val credentials = UserCredentialsRequest(
      universalId = stubber.creators.default().id.toUniversalFormat(),
      secret = "invalid",
      origin = Stubs.userCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `auth user succeeds with valid credentials`() {
    val credentials = UserCredentialsRequest(
      universalId = stubber.creators.default().id.toUniversalFormat(),
      secret = Stubs.userCredentialsRequest.secret,
      origin = Stubs.userCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.tokenValue }.isNotEmpty()
    }
  }

  @Test fun `get current token fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get token succeeds with valid authorization`() {
    val user = stubber.creators.default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<TokenDetailsResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        stubber.latestTokenOf(user).toNetwork()
      )
    }
  }

  @Test fun `get all tokens fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get all tokens succeeds with valid authorization`() {
    val token1 = stubber.creatorTokens().real(DEFAULT).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val token2 = stubber.creatorTokens().real(DEFAULT).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertThat(
      restTemplate.exchange<List<TokenDetailsResponse>>(
        url = "$baseUrl$TOKENS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token1),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(
        listOf(token1, token2).map {
          stubber.tokenDetailsOf(it).toNetwork()
        }
      )
    }
  }

  @Test fun `refresh user fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.PUT,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `refresh user succeeds with valid authorization`() {
    val token = stubber.creatorTokens().real(DEFAULT).token.tokenValue

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$AUTH",
        method = HttpMethod.PUT,
        requestEntity = bearerBlankRequest(token),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.tokenValue }.all {
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
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf("all" to null),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `unauth user (single token) succeeds with valid authorization`() {
    val token = stubber.creatorTokens().real(DEFAULT).token.tokenValue

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$AUTH?all={all}",
          method = HttpMethod.DELETE,
          requestEntity = bearerBlankRequest(token),
          uriVariables = mapOf("all" to false),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(stubber.isAuthorized(token)).isFalse()
    }
  }

  @Test fun `unauth user (all tokens) succeeds with valid authorization`() {
    val token1 = stubber.creatorTokens().real(DEFAULT).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val token2 = stubber.creatorTokens().real(DEFAULT).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$AUTH?all={all}",
          method = HttpMethod.DELETE,
          requestEntity = bearerBlankRequest(token1),
          uriVariables = mapOf("all" to true),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(stubber.isAuthorized(token1)).isFalse()
      assertThat(stubber.isAuthorized(token2)).isFalse()
    }
  }

  @Test fun `unauth tokens fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$TOKENS?tokenIds={token1}&tokenIds={token2}",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest("invalid"),
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
    val token1 = stubber.creatorTokens().real(DEFAULT).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val token2 = stubber.creatorTokens().real(DEFAULT).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val token3 = stubber.creatorTokens().real(DEFAULT).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))

    assertAll {
      assertThat(
        restTemplate.exchange<MessageResponse>(
          url = "$baseUrl$TOKENS?tokenIds={token1}&tokenIds={token2}",
          method = HttpMethod.DELETE,
          requestEntity = bearerBlankRequest(token3),
          uriVariables = mapOf(
            "token1" to token1,
            "token2" to token2,
          ),
        )
      ).all {
        transform { it.statusCode }.isEqualTo(HttpStatus.OK)
        transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      }

      assertThat(stubber.isAuthorized(token1)).isFalse()
      assertThat(stubber.isAuthorized(token2)).isFalse()
      assertThat(stubber.isAuthorized(token3)).isTrue()
    }
  }

}
