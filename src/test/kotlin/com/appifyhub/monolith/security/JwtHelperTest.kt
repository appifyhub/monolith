package com.appifyhub.monolith.security

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.security.JwtHelper.Claims
import java.util.Base64
import java.util.Date
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class JwtHelperTest {

  @Autowired
  private lateinit var helper: JwtHelper

  @Test fun `create jwt for claims contains the given data`() {
    val token = helper.createJwtForClaims(
      subject = "subject",
      claims = mapOf(
        "claim1" to "val1",
        "claim2" to "val2",
      ),
      createdAt = Date(100000),
      expiresAt = Date(200000),
    )

    val parts = token.split(".")
    val decoder = Base64.getDecoder()
    val type = decoder.decode(parts[0]).decodeToString()
    val content = decoder.decode(parts[1]).decodeToString()

    val issuedTime = TimeUnit.MILLISECONDS.toSeconds(Date(100000).time)
    val expirationTime = TimeUnit.MILLISECONDS.toSeconds(Date(200000).time)

    assertThat(type).isEqualTo("{\"typ\":\"JWT\",\"alg\":\"RS256\"}")
    assertThat(content).isEqualTo(
      "{\"sub\":\"subject\"," +
        "\"claim2\":\"val2\"," +
        "\"claim1\":\"val1\"," +
        "\"exp\":$expirationTime," +
        "\"iat\":$issuedTime}"
    )
  }

  @Test fun `extract properties from jwt extracts correctly`() {
    val token = helper.createJwtForClaims(
      subject = "subject",
      claims = mapOf(
        "claim1" to "val1",
        "claim2" to "val2",
      ),
      createdAt = Date(100000),
      expiresAt = Date(200000),
    )

    assertThat(helper.extractPropertiesFromJwt(token))
      .isEqualTo(
        hashMapOf(
          JwtClaimNames.SUB to "subject",
          "claim1" to "val1",
          "claim2" to "val2",
          Claims.CREATED_AT to TimeUnit.MILLISECONDS.toSeconds(Date(100000).time).toInt(),
          Claims.EXPIRES_AT to TimeUnit.MILLISECONDS.toSeconds(Date(200000).time).toInt(),
          Claims.VALUE to token,
        )
      )
  }

}
