package com.appifyhub.monolith.controller.creator

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.messaging.Variable
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.creator.messaging.MessageResponse
import com.appifyhub.monolith.network.creator.messaging.MessageTemplateResponse
import com.appifyhub.monolith.network.creator.messaging.VariableResponse
import com.appifyhub.monolith.network.creator.messaging.ops.DetectVariablesRequest
import com.appifyhub.monolith.network.creator.messaging.ops.MessageInputsRequest
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
import java.util.Locale

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
  classes = [TestAppifyHubApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class MessageTemplateControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var stubber: Stubber
  @Autowired lateinit var service: MessageTemplateService

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  // region Adding templates

  @Test fun `adding template fails when unauthorized`() {
    val request = Stubs.messageTemplateCreateRequest
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, "invalid"),
        uriVariables = mapOf("projectId" to Stubs.project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `adding template fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = Project.Status.REVIEW)
    val token = stubber.tokens(creator).real().token.tokenValue
    val request = Stubs.messageTemplateCreateRequest

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `adding template fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.messageTemplateCreateRequest

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `adding template works with valid inputs`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue
    val request = Stubs.messageTemplateCreateRequest

    assertThat(
      restTemplate.exchange<MessageTemplateResponse>(
        url = "$baseUrl${Endpoints.TEMPLATES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // service tests cover data checks, cover only basics here
      transform { it.body!!.id }
        .isGreaterThan(0)
      transform { it.body!!.name }
        .isEqualTo(request.name)
      transform { it.body!!.content }
        .isEqualTo(request.content)
    }
  }

  // endregion

  // region Fetching & Searching templates

  @Test fun `fetching template by ID fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to Stubs.project.id,
          "templateId" to Stubs.messageTemplate.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `fetching template by ID fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "templateId" to Stubs.messageTemplate.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `fetching template by ID works with valid inputs`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue
    val template = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))

    assertThat(
      restTemplate.exchange<MessageTemplateResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "templateId" to template.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }
        .isDataClassEqualTo(template.toNetwork())
    }
  }

  @Test fun `searching templates fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("projectId" to Stubs.project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `searching templates fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `searching templates works with valid name and language`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue

    // one matching, one mismatch
    val template = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))
    service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        languageTag = Locale.GERMAN.toLanguageTag(),
      ),
    )

    assertThat(
      restTemplate.exchange<List<MessageTemplateResponse>>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}?name=${template.name}&language_tag=${template.languageTag}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }
        .hasSize(1)
      transform { it.body!!.first() }
        .isDataClassEqualTo(template.toNetwork())
    }
  }

  @Test fun `searching templates works with valid name only`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue

    // one matching, one mismatch
    val template = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))
    service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, name = "mismatching_name"))

    assertThat(
      restTemplate.exchange<List<MessageTemplateResponse>>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}?name=${template.name}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }
        .hasSize(1)
      transform { it.body!!.first() }
        .isDataClassEqualTo(template.toNetwork())
    }
  }

  @Test fun `searching templates works with project ID only`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue
    val template1 = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, name = "name1"))
    val template2 = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, name = "name2"))

    assertThat(
      restTemplate.exchange<List<MessageTemplateResponse>>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }
        .hasSize(2)
      transform { it.body!!.first() }
        .isDataClassEqualTo(template1.toNetwork())
      transform { it.body!![1] }
        .isDataClassEqualTo(template2.toNetwork())
    }
  }

  // endregion

  // region Updating templates

  @Test fun `updating template fails when unauthorized`() {
    val request = Stubs.messageTemplateUpdateRequest
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, "invalid"),
        uriVariables = mapOf(
          "projectId" to Stubs.project.id,
          "templateId" to Stubs.messageTemplate.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `updating template fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.messageTemplateUpdateRequest

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "templateId" to Stubs.messageTemplate.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `updating template works with valid inputs`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue
    val template = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))
    val request = Stubs.messageTemplateUpdateRequest

    assertThat(
      restTemplate.exchange<MessageTemplateResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "templateId" to template.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // service tests cover data checks, cover only basics here
      transform { it.body!!.id }
        .isEqualTo(template.id)
      transform { it.body!!.name }
        .isEqualTo(request.name?.value)
      transform { it.body!!.content }
        .isEqualTo(request.content?.value)
    }
  }

  // endregion

  // region Deleting templates

  @Test fun `deleting template by ID fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to Stubs.project.id,
          "templateId" to Stubs.messageTemplate.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `deleting template by ID fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "templateId" to Stubs.messageTemplate.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `deleting template by ID works with valid inputs`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue
    val template = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "templateId" to template.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { service.fetchTemplatesByProjectId(project.id) }
        .isEmpty()
    }
  }

  @Test fun `deleting templates fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("projectId" to Stubs.project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `deleting templates fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `deleting templates works with valid name`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue

    // one matching, one mismatch
    val remove = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))
    val keep = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, name = "mismatching_name"))

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}?name=${remove.name}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { service.fetchTemplatesByProjectId(project.id) }
        .all {
          hasSize(1)
          transform { it.first().id }.isEqualTo(keep.id)
        }
    }
  }

  @Test fun `deleting templates works with project ID only`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue

    service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, name = "name1"))
    service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, name = "name2"))

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_SEARCH}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { service.fetchTemplatesByProjectId(project.id) }
        .isEmpty()
    }
  }

  // endregion

  // region Variable Detection

  @Test fun `getting defined variables fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_VARIABLES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("projectId" to Stubs.project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `getting defined variables fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_VARIABLES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `getting defined variables works with valid inputs`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<List<VariableResponse>>(
        url = "$baseUrl${Endpoints.TEMPLATE_VARIABLES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.first().code }
        .isEqualTo(Variable.USER_NAME.code)
      transform { it.body!!.first().example }
        .isEqualTo(Variable.USER_NAME.example)
    }
  }

  @Test fun `detecting variables fails when unauthorized`() {
    val request = DetectVariablesRequest("")
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_VARIABLES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, "invalid"),
        uriVariables = mapOf("projectId" to Stubs.project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `detecting variables fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = DetectVariablesRequest("")

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_VARIABLES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `detecting variables works with valid inputs`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue
    val request = DetectVariablesRequest("invalid {{also_invalid}} {{${Variable.USER_NAME.code}}}")

    assertThat(
      restTemplate.exchange<List<VariableResponse>>(
        url = "$baseUrl${Endpoints.TEMPLATE_VARIABLES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!! }
        .hasSize(1)
      transform { it.body!!.first().code }
        .isEqualTo(Variable.USER_NAME.code)
      transform { it.body!!.first().example }
        .isEqualTo(Variable.USER_NAME.example)
    }
  }

  // endregion

  // region Materializing

  @Test fun `materialize template fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_MATERIALIZE}",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("projectId" to Stubs.project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `materialize template fails when not project creator`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = MessageInputsRequest()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_MATERIALIZE}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `materialize template fails without template chosen`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue
    val request = MessageInputsRequest()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_MATERIALIZE}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
    }
  }

  @Test fun `materialize template (by ID) works with user ID in input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val token = stubber.tokens(creator).real().token.tokenValue

    val template = service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        content = "OK for {{${Variable.USER_NAME.code}}}",
      ),
    )
    val target = stubber.users(project).default()
    val request = MessageInputsRequest(universalUserId = target.id.toUniversalFormat())

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_MATERIALIZE}?id=${template.id}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.template.id }
        .isEqualTo(template.id)
      transform { it.body!!.materialized }
        .isEqualTo("OK for ${target.name}")
    }
  }

  @Test fun `materialize template (by name) works with project ID in input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true, name = "PRO-J")
    val token = stubber.tokens(creator).real().token.tokenValue

    val template = service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        content = "OK for {{${Variable.PROJECT_NAME.code}}}",
      ),
    )
    val request = MessageInputsRequest(projectId = project.id)

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl${Endpoints.TEMPLATE_MATERIALIZE}?name=${template.name}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.template.id }
        .isEqualTo(template.id)
      transform { it.body!!.materialized }
        .isEqualTo("OK for ${project.name}")
    }
  }

  // endregion

}
