package com.appifyhub.monolith.features.creator.api

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints.PROJECT
import com.appifyhub.monolith.controller.common.Endpoints.PROJECTS
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.features.auth.domain.access.AccessManager
import com.appifyhub.monolith.features.creator.api.model.ProjectResponse
import com.appifyhub.monolith.features.creator.api.model.ProjectUpdateRequest
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.network.common.SettableRequest
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBodyRequest
import com.appifyhub.monolith.util.bearerEmptyRequest
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
class CreatorProjectControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var stubber: Stubber
  @Autowired lateinit var accessManager: AccessManager

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `create a project fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.POST,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `create a project fails when not creator`() {
    val project = stubber.projects.new()
    val user = stubber.users(project).owner()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.projectCreateRequest.copy(ownerUniversalId = user.id.toUniversalFormat())

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `create a project fails when mismatching owner`() {
    val creator1 = stubber.creators.default()
    val creator2 = stubber.creators.default(idSuffix = "_other")
    val token = stubber.tokens(creator1).real().token.tokenValue
    val request = Stubs.projectCreateRequest.copy(ownerUniversalId = creator2.id.toUniversalFormat())

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `create a project succeeds`() {
    val creator = stubber.creators.default()
    val token = stubber.tokens(creator).real().token.tokenValue
    val request = Stubs.projectCreateRequest.copy(
      ownerUniversalId = creator.id.toUniversalFormat(),
      logoUrl = null,
      websiteUrl = null,
    )

    assertThat(
      restTemplate.exchange<ProjectResponse>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.state.status }
        .isEqualTo(Project.Status.REVIEW.toString())
      transform { it.body!!.userIdType }
        .isEqualTo(Stubs.projectResponse.userIdType)
      transform { it.body!!.projectId }
        .isNotEqualTo(stubber.projects.creator().id)
    }
  }

  @Test fun `get all projects fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get all projects fails when not super-creator`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get all projects succeeds`() {
    val projectResponse = stubber.projects.new().let {
      it.toNetwork(projectState = accessManager.fetchProjectState(it.id))
    }
    val creatorProjectResponse = stubber.projects.creator().let {
      it.toNetwork(projectState = accessManager.fetchProjectState(it.id))
    }
    val token = stubber.creatorTokens().real(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<List<ProjectResponse>>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.first().projectId }
        .isEqualTo(creatorProjectResponse.projectId)
      transform { it.body!!.first().state.status }
        .isEqualTo(creatorProjectResponse.state.status)

      transform { it.body!![1].projectId }
        .isEqualTo(projectResponse.projectId)
      transform { it.body!![1].state.status }
        .isEqualTo(projectResponse.state.status)
    }
  }

  @Test fun `get creator's projects fails when unauthorized`() {
    val creator = stubber.creators.default()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS?creator_id={creator_id}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("creator_id" to creator.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get creator's projects fails when not owner or super-creator`() {
    val creator1 = stubber.creators.default()
    val creator2 = stubber.creators.default(idSuffix = "_other")
    val token = stubber.tokens(creator1).real().token.tokenValue
    stubber.projects.new(owner = creator1)
    stubber.projects.new(owner = creator2)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS?creator_id={creator_id}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("creator_id" to creator2.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get creator's projects succeeds when requester is owner`() {
    val superCreator = stubber.creators.owner()
    stubber.projects.new(owner = superCreator)
    val creator = stubber.creators.default()
    val projectResponse = stubber.projects.new(owner = creator).let {
      it.toNetwork(projectState = accessManager.fetchProjectState(it.id))
    }
    val token = stubber.tokens(creator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<List<ProjectResponse>>(
        url = "$baseUrl$PROJECTS?creator_id={creator_id}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("creator_id" to creator.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!! }
        .hasSize(1)
      transform { it.body!!.first().projectId }
        .isEqualTo(projectResponse.projectId)
      transform { it.body!!.first().state.status }
        .isEqualTo(projectResponse.state.status)
    }
  }

  @Test fun `get creator's projects succeeds when requester is super-creator`() {
    val superCreator = stubber.creators.owner()
    stubber.projects.new(owner = superCreator)
    val creator = stubber.creators.default()
    val projectResponse = stubber.projects.new(owner = creator).let {
      it.toNetwork(projectState = accessManager.fetchProjectState(it.id))
    }
    val token = stubber.tokens(superCreator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<List<ProjectResponse>>(
        url = "$baseUrl$PROJECTS?creator_id={creator_id}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("creator_id" to creator.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!! }
        .hasSize(1)
      transform { it.body!!.first().projectId }
        .isEqualTo(projectResponse.projectId)
      transform { it.body!!.first().state.status }
        .isEqualTo(projectResponse.state.status)
    }
  }

  @Test fun `get any project fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get any project succeeds`() {
    val project = stubber.projects.new()
    val projectResponse = project.toNetwork(projectState = accessManager.fetchProjectState(project.id))
    val token = stubber.tokens(project).real(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<ProjectResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.projectId }
        .isEqualTo(projectResponse.projectId)
      transform { it.body!!.state.status }
        .isEqualTo(projectResponse.state.status)
    }
  }

  @Test fun `update project fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.PUT,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `update project fails when status changes and not super-creator`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = Project.Status.REVIEW)
    val token = stubber.tokens(creator).real().token.tokenValue
    val request = ProjectUpdateRequest(
      type = null,
      status = SettableRequest(Project.Status.ACTIVE.toString()),
    )

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `update project fails when type changes and not the owner`() {
    val creator1 = stubber.creators.default()
    val creator2 = stubber.creators.default(idSuffix = "_other")
    val project = stubber.projects.new(owner = creator1, status = Project.Status.REVIEW)
    val token = stubber.tokens(creator2).real().token.tokenValue
    val request = ProjectUpdateRequest(
      type = SettableRequest(Project.Type.FREE.toString()),
      status = null,
    )

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `update project succeeds when owner changes type`() {
    val creator = stubber.creators.default()
    val projectResponse = stubber.projects.new(owner = creator, status = Project.Status.REVIEW).let {
      it.toNetwork(projectState = accessManager.fetchProjectState(it.id))
    }
    val token = stubber.tokens(creator).real().token.tokenValue
    val request = ProjectUpdateRequest(
      type = SettableRequest(Project.Type.FREE.toString()),
      status = null,
    )

    assertThat(
      restTemplate.exchange<ProjectResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to projectResponse.projectId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.projectId }
        .isEqualTo(projectResponse.projectId)
      transform { it.body!!.state.status }
        .isEqualTo(projectResponse.state.status)
      transform { it.body!!.type }
        .isEqualTo(Project.Type.FREE.toString())
    }
  }

  @Test fun `update project succeeds when super-creator changes type`() {
    val creator = stubber.creators.default()
    val superCreator = stubber.creators.owner()
    val projectResponse = stubber.projects.new(owner = creator, status = Project.Status.REVIEW).let {
      it.toNetwork(projectState = accessManager.fetchProjectState(it.id))
    }
    val token = stubber.tokens(superCreator).real().token.tokenValue
    val request = ProjectUpdateRequest(
      type = SettableRequest(Project.Type.FREE.toString()),
      status = null,
    )

    assertThat(
      restTemplate.exchange<ProjectResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to projectResponse.projectId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.projectId }
        .isEqualTo(projectResponse.projectId)
      transform { it.body!!.state.status }
        .isEqualTo(projectResponse.state.status)
      transform { it.body!!.type }
        .isEqualTo(Project.Type.FREE.toString())
    }
  }

  @Test fun `update project succeeds when super-creator changes status`() {
    val creator = stubber.creators.default()
    val superCreator = stubber.creators.owner()
    val projectResponse = stubber.projects.new(owner = creator, status = Project.Status.REVIEW).let {
      it.toNetwork(projectState = accessManager.fetchProjectState(it.id))
    }
    val token = stubber.tokens(superCreator).real().token.tokenValue
    val request = ProjectUpdateRequest(
      type = null,
      status = SettableRequest(Project.Status.ACTIVE.toString()),
    )

    assertThat(
      restTemplate.exchange<ProjectResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to projectResponse.projectId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.projectId }
        .isEqualTo(projectResponse.projectId)
      transform { it.body!!.state.status }
        .isEqualTo(Project.Status.ACTIVE.toString())
      transform { it.body!!.type }
        .isEqualTo(projectResponse.type)
    }
  }

  @Test fun `update project succeeds when super-creator changes type and status`() {
    val creator = stubber.creators.default()
    val superCreator = stubber.creators.owner()
    val projectResponse = stubber.projects.new(owner = creator, status = Project.Status.REVIEW).let {
      it.toNetwork(projectState = accessManager.fetchProjectState(it.id))
    }
    val token = stubber.tokens(superCreator).real().token.tokenValue
    val request = ProjectUpdateRequest(
      type = SettableRequest(Project.Type.FREE.toString()),
      status = SettableRequest(Project.Status.ACTIVE.toString()),
    )

    assertThat(
      restTemplate.exchange<ProjectResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf(
          "projectId" to projectResponse.projectId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic here, working around...
      transform { it.body!!.projectId }
        .isEqualTo(projectResponse.projectId)
      transform { it.body!!.state.status }
        .isEqualTo(Project.Status.ACTIVE.toString())
      transform { it.body!!.type }
        .isEqualTo(Project.Type.FREE.toString())
    }
  }

  @Test fun `remove project fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `remove project succeeds when requester is owner`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator)
    val token = stubber.tokens(creator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }
        .isEqualTo(SimpleResponse.DONE)
      assertThat(stubber.projects.all.firstOrNull { it.id == project.id })
        .isNull()
    }
  }

  @Test fun `remove project succeeds when requester is super-creator`() {
    val creator = stubber.creators.default()
    val superCreator = stubber.creators.owner()
    val project = stubber.projects.new(owner = creator)
    val token = stubber.tokens(superCreator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }
        .isEqualTo(SimpleResponse.DONE)
      assertThat(stubber.projects.all.firstOrNull { it.id == project.id })
        .isNull()
    }
  }

  @Test fun `remove all creator's projects fails when unauthorized`() {
    val creator = stubber.creators.default()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS?creator_id={creator_id}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("creator_id" to creator.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `remove all creator's projects succeeds when requester is owner`() {
    val creator = stubber.creators.default()
    val project1 = stubber.projects.new(owner = creator)
    val project2 = stubber.projects.new(owner = creator)
    val token = stubber.tokens(creator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS?creator_id={creator_id}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("creator_id" to creator.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }
        .isEqualTo(SimpleResponse.DONE)
      assertThat(stubber.projects.all.firstOrNull { it.id in setOf(project1.id, project2.id) })
        .isNull()
    }
  }

  @Test fun `remove all creator's projects succeeds when requester is super-creator`() {
    val creator = stubber.creators.default()
    val superCreator = stubber.creators.owner()
    val project1 = stubber.projects.new(owner = creator)
    val project2 = stubber.projects.new(owner = creator)
    val token = stubber.tokens(superCreator).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECTS?creator_id={creator_id}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("creator_id" to creator.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }
        .isEqualTo(SimpleResponse.DONE)
      assertThat(stubber.projects.all.firstOrNull { it.id in setOf(project1.id, project2.id) })
        .isNull()
    }
  }

}
