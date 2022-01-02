package com.appifyhub.monolith.controller.creator

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints.UNIVERSAL_USER_FORCE_VERIFY
import com.appifyhub.monolith.domain.creator.Project.Status.REVIEW
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.util.Stubber
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

  @Test fun `token force-verification fails when project non-functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = REVIEW)
    val user = stubber.users(project).default(autoVerified = false)
    val token = stubber.tokens(creator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$UNIVERSAL_USER_FORCE_VERIFY",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `token force-verification succeeds with valid token for lower rank`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, forceBasicProps = true)
    val user = stubber.users(project).default(autoVerified = false)
    val token = stubber.tokens(creator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$UNIVERSAL_USER_FORCE_VERIFY",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)
      assertThat(stubber.users(project).default().verificationToken).isNull()
    }
  }

}
