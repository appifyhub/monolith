package com.appifyhub.monolith.controller.user

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints.ANY_USER_SIGNUP
import com.appifyhub.monolith.controller.common.Endpoints.ANY_USER_UNIVERSAL
import com.appifyhub.monolith.domain.creator.Project.Status.REVIEW
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerEmptyRequest
import com.appifyhub.monolith.util.bodyRequest
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
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
  classes = [TestAppifyHubApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Suppress("SpringJavaInjectionPointsAutowiringInspection") // some weird thing with restTemplate
class UserControllerTest {

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

  // region Add User

  @Test fun `add user fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_USER_SIGNUP",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(Stubs.userSignupRequest),
        uriVariables = mapOf("projectId" to project.id),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `add user succeeds`() {
    val project = stubber.projects.new(forceBasicProps = true)

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$ANY_USER_SIGNUP",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(Stubs.userSignupRequest),
        uriVariables = mapOf("projectId" to project.id),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = Stubs.userId.userId,
          projectId = project.id,
          universalId = Stubs.userId.copy(projectId = project.id).toUniversalFormat(),
          authority = Authority.DEFAULT.name,
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        )
      )
    }
  }

  // endregion

  // region Get User

  @Test fun `get user fails when unauthorized`() {
    val project = stubber.projects.new()
    val universalId = stubber.users(project).default().id.toUniversalFormat()

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_USER_UNIVERSAL",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("universalId" to universalId),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get user fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_USER_UNIVERSAL",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `get user succeeds with valid authorization`() {
    val project = stubber.projects.new(forceBasicProps = true)
    val self = stubber.users(project).default()
    val universalId = self.id.toUniversalFormat()
    val token = stubber.tokens(self).real().token.tokenValue

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$ANY_USER_UNIVERSAL",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to universalId),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = self.id.userId,
          projectId = project.id,
          universalId = universalId,
          type = self.type.name,
          authority = self.authority.name,
          birthday = DateTimeMapper.formatAsDate(self.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        )
      )
    }
  }

  // endregion

}
