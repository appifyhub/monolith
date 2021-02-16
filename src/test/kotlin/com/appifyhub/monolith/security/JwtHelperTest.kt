package com.appifyhub.monolith.security

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.Base64
import java.util.concurrent.TimeUnit

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class JwtHelperTest {

  @Autowired
  private lateinit var helper: JwtHelper

  @Autowired
  private lateinit var timeProvider: TimeProviderFake

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeUnit.DAYS.toMillis(10L) }
  }

  @BeforeEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `jwt contains given data`() {
    val token = helper.createJwtForClaims("subject", mapOf("claim1" to "val1", "claim2" to "val2"))

    val parts = token.split(".")
    val decoder = Base64.getDecoder()
    val type = decoder.decode(parts[0]).decodeToString()
    val content = decoder.decode(parts[1]).decodeToString()

    val issuedTime = TimeUnit.MILLISECONDS.toSeconds(timeProvider.staticTime()!!)
    val expirationTime = TimeUnit.MILLISECONDS.toSeconds(timeProvider.staticTime()!!) + TimeUnit.DAYS.toSeconds(90)

    assertThat(type).isEqualTo("{\"typ\":\"JWT\",\"alg\":\"RS256\"}")
    assertThat(content).isEqualTo(
      "{\"sub\":\"subject\",\"nbf\":$issuedTime," +
        "\"claim2\":\"val2\",\"claim1\":\"val1\",\"exp\":$expirationTime," +
        "\"iat\":$issuedTime}"
    )
  }

}