package com.appifyhub.monolith.controller.messaging

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.network.messaging.PushDeviceResponse
import com.appifyhub.monolith.network.messaging.PushDevicesResponse
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.messaging.PushDeviceService
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
class PushDevicesControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var stubber: Stubber
  @Autowired lateinit var service: PushDeviceService
  @Autowired lateinit var creatorService: CreatorService

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  // region Adding push devices

  @Test fun `adding a push device fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = Project.Status.REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.pushDeviceRequest

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `adding a push device fails when push not configured`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.pushDeviceRequest

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `adding a push device fails when unauthorized`() {
    val request = Stubs.pushDeviceRequest
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, "invalid"),
        uriVariables = mapOf("universalId" to Stubs.user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `adding a push device works with valid input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true).enablePush()
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = Stubs.pushDeviceRequest

    assertThat(
      restTemplate.exchange<PushDeviceResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }.isDataClassEqualTo(Stubs.pushDeviceResponse)
    }
  }

  // endregion

  // region Fetching one push device

  @Test fun `fetching one push device fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = Project.Status.REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICE}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
          "deviceId" to Stubs.pushDevice.deviceId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `fetching one push device fails when push not configured`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICE}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
          "deviceId" to Stubs.pushDevice.deviceId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `fetching one push device fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICE}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "universalId" to Stubs.user.id.toUniversalFormat(),
          "deviceId" to Stubs.pushDevice.deviceId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `fetching one push device works with valid input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true).enablePush()
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    service.addDevice(Stubs.pushDevice.copy(owner = user))

    assertThat(
      restTemplate.exchange<PushDeviceResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICE}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
          "deviceId" to Stubs.pushDevice.deviceId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }.isDataClassEqualTo(Stubs.pushDeviceResponse)
    }
  }

  // endregion

  // region Fetching all push devices

  @Test fun `fetching all push devices fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = Project.Status.REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `fetching all push devices fails when push not configured`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `fetching all push devices fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("universalId" to Stubs.user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `fetching all push devices works with valid input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true).enablePush()
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    service.addDevice(Stubs.pushDevice.copy(owner = user))

    assertThat(
      restTemplate.exchange<PushDevicesResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)

      transform { it.body!! }.isDataClassEqualTo(Stubs.pushDevicesResponse)
    }
  }

  // endregion

  // region Removing one push device

  @Test fun `removing one push device fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = Project.Status.REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICE}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
          "deviceId" to Stubs.pushDevice.deviceId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `removing one push device fails when push not configured`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICE}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
          "deviceId" to Stubs.pushDevice.deviceId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `removing one push device fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICE}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "universalId" to Stubs.user.id.toUniversalFormat(),
          "deviceId" to Stubs.pushDevice.deviceId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `removing one push device works with valid input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true).enablePush()
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    val device = service.addDevice(Stubs.pushDevice.copy(owner = user))

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICE}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
          "deviceId" to Stubs.pushDevice.deviceId,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      assertThat { service.fetchDeviceById(device.deviceId) }.isFailure()
    }
  }

  // endregion

  // region Removing all push devices

  @Test fun `removing all push devices fails when project not functional`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, status = Project.Status.REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `removing all push devices fails when push not configured`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `removing all push devices fails when unauthorized`() {
    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("universalId" to Stubs.user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `removing all push devices works with valid input`() {
    val creator = stubber.creators.default()
    val project = stubber.projects.new(owner = creator, activateNow = true).enablePush()
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    service.addDevice(Stubs.pushDevice.copy(owner = user))

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl${Endpoints.UNIVERSAL_USER_PUSH_DEVICES}",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      assertThat(service.fetchAllDevicesByUser(user)).isEmpty()
    }
  }

  // endregion

  // region Helpers

  private fun Project.enablePush() = creatorService.updateProject(
    ProjectUpdater(
      id = id,
      firebaseConfig = Settable(Stubs.project.firebaseConfig),
    ),
  )

  // endregion

}
