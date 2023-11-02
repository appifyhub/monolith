package com.appifyhub.monolith.controller.messaging

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.Project.Status.REVIEW
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.integrations.MailgunConfig
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.integrations.CommunicationsService.Type.PUSH
import com.appifyhub.monolith.service.messaging.MessageTemplateService
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBodyRequest
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
class CommunicationsControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var stubber: Stubber
  @Autowired lateinit var creatorService: CreatorService
  @Autowired lateinit var templateService: MessageTemplateService

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `sending a message fails when neither template ID nor template name are provided`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.messageSendRequest.copy(templateId = null, templateName = null)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.MESSAGING_SEND}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "universalId" to user.id.toUniversalFormat(),
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `sending a message fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.messageSendRequest

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.MESSAGING_SEND}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "universalId" to user.id.toUniversalFormat(),
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `sending a push message fails when push not configured`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.messageSendRequest.copy(type = PUSH.name)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.MESSAGING_SEND}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "universalId" to user.id.toUniversalFormat(),
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `sending a message fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.MESSAGING_SEND}",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to Stubs.project.id,
          "universalId" to Stubs.user.id.toUniversalFormat(),
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `sending a message works when configured properly`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true).enableEmails()
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue
    val template = templateService.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))
    val request = Stubs.messageSendRequest.copy(templateId = null, templateName = template.name)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.MESSAGING_SEND}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "universalId" to user.id.toUniversalFormat(),
        ),
      ),
    ).all {
      // Mailgun should reject because of fake credential, but that's proof that emails were sent
      // And yes, this is not great. There should be some kind of "dry run" flag… but life is short
      transform { it.statusCode }.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
      transform { it.body!! }.transform { it.message }.contains("401 Unauthorized")
    }
  }

  // Helpers

  private fun Project.enableEmails() = creatorService.updateProject(
    ProjectUpdater(
      id = id,
      mailgunConfig = Settable(
        MailgunConfig(
          apiKey = "fake",
          domain = "fake",
          senderName = "fake",
          senderEmail = "fake@mailgun.com",
        ),
      ),
    ),
  )

}
