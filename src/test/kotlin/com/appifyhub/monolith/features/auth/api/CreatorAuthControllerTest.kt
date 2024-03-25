package com.appifyhub.monolith.features.auth.api

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints.CREATOR_API_KEY
import com.appifyhub.monolith.controller.common.Endpoints.CREATOR_AUTH
import com.appifyhub.monolith.features.user.domain.model.User.Authority.OWNER
import com.appifyhub.monolith.features.auth.api.model.TokenResponse
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.features.creator.api.model.user.ApiKeyRequest
import com.appifyhub.monolith.features.creator.api.model.user.CreatorCredentialsRequest
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBodyRequest
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
import org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
  classes = [TestAppifyHubApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class CreatorAuthControllerTest {

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

  @Test fun `auth creator fails with invalid credentials`() {
    val credentials = CreatorCredentialsRequest(
      universalId = stubber.creators.default().id.toUniversalFormat(),
      signature = "invalid",
      origin = Stubs.creatorCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$CREATOR_AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `auth user succeeds with valid credentials`() {
    val credentials = CreatorCredentialsRequest(
      universalId = stubber.creators.default().id.toUniversalFormat(),
      signature = Stubs.creatorCredentialsRequest.signature,
      origin = Stubs.creatorCredentialsRequest.origin,
    )

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$CREATOR_AUTH",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(credentials),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.tokenValue }.isNotEmpty()
    }
  }

  @Test fun `create api key fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$CREATOR_API_KEY",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `create api key succeeds with valid credentials`() {
    val ownerToken = stubber.creatorTokens().real(OWNER).token.tokenValue
    timeProvider.advanceBy(Duration.ofHours(1))
    val keyData = ApiKeyRequest(origin = Stubs.creatorCredentialsRequest.origin)

    assertThat(
      restTemplate.exchange<TokenResponse>(
        url = "$baseUrl$CREATOR_API_KEY",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(keyData, ownerToken),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.tokenValue }.isNotEmpty()
    }
  }

}
