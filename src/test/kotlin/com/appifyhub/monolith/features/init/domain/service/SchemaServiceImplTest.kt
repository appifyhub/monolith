package com.appifyhub.monolith.features.init.domain.service

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.features.init.domain.model.Schema
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.MethodMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class SchemaServiceImplTest {

  @Autowired lateinit var service: SchemaService

  @Test fun `update fails with invalid schema version`() {
    assertFailure { service.update(Schema(-1, false)) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Schema Version")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `update succeeds with valid schema version`() {
    assertThat(service.update(Schema(2, true)))
      .isEqualTo(Unit)
  }

  @Test fun `is initialized fails with invalid schema version`() {
    assertFailure { service.isInitialized(-1) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Schema Version")
      }
  }

  @Test fun `is initialized is true for initialized schemas`() {
    // initial schema (1) initializes with context load
    assertThat(service.isInitialized(1))
      .isTrue()
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `is initialized is false for uninitialized schemas`() {
    assertThat(service.isInitialized(2))
      .isFalse()
  }

}
