package com.appifyhub.monolith.features.auth.domain.security

import assertk.assertThat
import assertk.assertions.isNotNull
import com.appifyhub.monolith.TestAppifyHubApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class JwtConfigurationTest {

  @Autowired
  private lateinit var configuration: JwtConfiguration

  @Test fun `key store exists`() {
    assertThat(configuration.keyStore()).isNotNull()
  }

  @Test fun `jwt signing key exists`() {
    val keyStore = configuration.keyStore()
    assertThat(configuration.jwtSigningKey(keyStore)).isNotNull()
  }

  @Test fun `jwt validation key exists`() {
    val keyStore = configuration.keyStore()
    assertThat(configuration.jwtValidationKey(keyStore)).isNotNull()
  }

  @Test fun `jwt decoder exists`() {
    val keyStore = configuration.keyStore()
    val publicKey = configuration.jwtValidationKey(keyStore)
    assertThat(configuration.jwtDecoder(publicKey)).isNotNull()
  }

}
