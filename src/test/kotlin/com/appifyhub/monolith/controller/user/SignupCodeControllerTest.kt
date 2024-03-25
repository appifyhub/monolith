package com.appifyhub.monolith.controller.user

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.features.creator.domain.model.Project.Status.REVIEW
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.SignupCodeResponse
import com.appifyhub.monolith.network.user.SignupCodesResponse
import com.appifyhub.monolith.repository.user.SignupCodeGenerator
import com.appifyhub.monolith.service.user.SignupCodeService
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerEmptyRequest
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

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
  classes = [TestAppifyHubApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class SignupCodeControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var stubber: Stubber
  @Autowired lateinit var service: SignupCodeService

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
    SignupCodeGenerator.interceptor = { null }
  }

  // region Creating signup codes

  @Test fun `creating a signup code fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_SIGNUP_CODES}",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `adding a push device fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_SIGNUP_CODES}",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("universalId" to Stubs.user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `creating a signup code works with valid input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    SignupCodeGenerator.interceptor = { "FAKE-CODE-1234" }
    assertThat(
      restTemplate.exchange<SignupCodeResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_SIGNUP_CODES}",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      val expected = SignupCodeResponse(
        code = "FAKE-CODE-1234",
        isUsed = false,
        createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        usedAt = null,
      )
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(expected)
    }
  }

  // endregion

  // region Fetching all signup codes

  @Test fun `fetching all signup codes fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_SIGNUP_CODES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `fetching all signup codes fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_SIGNUP_CODES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("universalId" to Stubs.user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `fetching all signup codes works with valid input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true, maxSignupCodesPerUser = 5)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    SignupCodeGenerator.interceptor = { "FAKE-CODE-1234" }
    val signupCode1 = service.createCode(user.id).toNetwork()
    SignupCodeGenerator.interceptor = { "FAKE-CODE-5678" }
    val signupCode2 = service.createCode(user.id).let { service.markCodeUsed(it.code, project.id) }.toNetwork()

    assertThat(
      restTemplate.exchange<SignupCodesResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_SIGNUP_CODES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      val expected = SignupCodesResponse(
        signupCodes = listOf(signupCode1, signupCode2),
        maxSignupCodes = 5,
      )
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(expected)
    }
  }

  // endregion

}
