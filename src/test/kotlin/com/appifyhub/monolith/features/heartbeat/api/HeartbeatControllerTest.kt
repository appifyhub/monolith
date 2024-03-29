package com.appifyhub.monolith.features.heartbeat.api

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.features.common.api.Endpoints.HEARTBEAT
import com.appifyhub.monolith.features.heartbeat.api.model.HeartbeatResponse
import com.appifyhub.monolith.features.user.api.DateTimeMapper
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.emptyRequest
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
class HeartbeatControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { DateTimeMapper.parseAsDateTime("2021-09-08 07:06").time }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `beat responds with current time`() {
    assertThat(
      restTemplate.exchange<HeartbeatResponse>(
        url = "$baseUrl/$HEARTBEAT",
        method = HttpMethod.GET,
        requestEntity = emptyRequest(),
        uriVariables = emptyUriVariables(),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        HeartbeatResponse(
          beat = timeProvider.currentInstant,
          ip = null,
          geo = null,
          version = "1.0.1.test", // from test properties
        ),
      )
    }
  }

}
