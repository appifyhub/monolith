package com.appifyhub.monolith.controller.admin

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.admin.AdminUserController.Endpoints.ANY_USER
import com.appifyhub.monolith.domain.user.User.Authority.DEFAULT
import com.appifyhub.monolith.domain.user.User.Authority.MODERATOR
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.util.AuthTestHelper
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBlankRequest
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
class AdminUserControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var authHelper: AuthTestHelper

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `get any user fails when unauthorized`() {
    val userId = authHelper.defaultUser.id

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_USER",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to userId.projectId,
          "userId" to userId.userId,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get any user succeeds for self`() {
    val user = authHelper.defaultUser
    val token = authHelper.newRealJwt(DEFAULT).token.tokenValue

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$ANY_USER",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to user.id.projectId,
          "userId" to user.id.userId,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = user.id.userId,
          universalId = user.id.toUniversalFormat(),
          type = user.type.name,
          authority = user.authority.name,
          birthday = DateTimeMapper.formatAsDate(user.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        )
      )
    }
  }

  @Test fun `get any user succeeds for lower rank`() {
    val user = authHelper.defaultUser
    val token = authHelper.newRealJwt(MODERATOR).token.tokenValue

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$ANY_USER",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to user.id.projectId,
          "userId" to user.id.userId,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = user.id.userId,
          universalId = user.id.toUniversalFormat(),
          type = user.type.name,
          authority = user.authority.name,
          birthday = DateTimeMapper.formatAsDate(user.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        )
      )
    }
  }

}
