package com.appifyhub.monolith.features.creator.api

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints.CREATOR_SIGNUP
import com.appifyhub.monolith.controller.common.Endpoints.UNIVERSAL_USER_FORCE_VERIFY
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.creator.domain.model.Project.Status.REVIEW
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.util.Stubber
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
class CreatorUserControllerTest {

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

  // region Add Creator

  @Test fun `add creator succeeds`() {
    val request = Stubs.creatorSignupRequest.copy(userId = "username@example.com")

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$CREATOR_SIGNUP",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(request),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = request.userId,
          projectId = stubber.projects.creator().id,
          universalId = UserId(request.userId, stubber.projects.creator().id).toUniversalFormat(),
          authority = User.Authority.DEFAULT.name,
          contact = request.userId,
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
    }
  }

  // endregion

  // region Force Verification

  @Test fun `token force-verification fails when project non-functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = REVIEW)
    val user = stubber.users(project).default(autoVerified = false)
    val token = stubber.tokens(creator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_FORCE_VERIFY",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `token force-verification succeeds with valid token for lower rank`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default(autoVerified = false)
    val token = stubber.tokens(creator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_FORCE_VERIFY",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(SimpleResponse.DONE)
      assertThat(stubber.users(project).default().verificationToken).isNull()
    }
  }

  // endregion

}
