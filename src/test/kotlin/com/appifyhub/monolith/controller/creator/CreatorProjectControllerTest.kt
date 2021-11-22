package com.appifyhub.monolith.controller.creator

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.creator.CreatorProjectController.Endpoints.ANY_PROJECT
import com.appifyhub.monolith.controller.creator.CreatorProjectController.Endpoints.PROJECTS
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.creator.ProjectResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBlankRequest
import com.appifyhub.monolith.util.blankUriVariables
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

  @Test fun `get all projects fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get all projects fails when not super owner`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get all projects succeeds`() {
    val projectResponse = stubber.projects.new().let {
      it.toNetwork(projectStatus = accessManager.fetchProjectStatus(it.id))
    }
    val creatorProjectResponse = stubber.projects.creator().let {
      it.toNetwork(projectStatus = accessManager.fetchProjectStatus(it.id))
    }
    val token = stubber.creatorTokens().real(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<List<ProjectResponse>>(
        url = "$baseUrl$PROJECTS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = blankUriVariables(),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic for comparisons, so just comparing the basics
      transform { it.body!!.first().projectId to it.body!!.first().status.status }
        .isEqualTo(creatorProjectResponse.projectId to creatorProjectResponse.status.status)
      transform { it.body!![1].projectId to it.body!![1].status.status }
        .isEqualTo(projectResponse.projectId to projectResponse.status.status)
    }
  }

  @Test fun `get any project fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$ANY_PROJECT",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get any project succeeds`() {
    val project = stubber.projects.new()
    val projectResponse = project.toNetwork(projectStatus = accessManager.fetchProjectStatus(project.id))
    val token = stubber.tokens(project).real(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<ProjectResponse>(
        url = "$baseUrl$ANY_PROJECT",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      // list ordering is problematic for comparisons, so just comparing the basics
      transform { it.body!!.projectId to it.body!!.status.status }
        .isEqualTo(projectResponse.projectId to projectResponse.status.status)
    }
  }

}
