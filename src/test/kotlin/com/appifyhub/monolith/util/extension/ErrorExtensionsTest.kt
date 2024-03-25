package com.appifyhub.monolith.util.extension

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import com.appifyhub.monolith.features.common.validation.Normalizer
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ErrorExtensionsTest {

  @Test fun `throwUnauthorized throws correctly`() {
    assertFailure { throwUnauthorized { "Message" } }
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.UNAUTHORIZED)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwNormalization throws correctly`() {
    assertFailure { throwNormalization { "Message" } }
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwLocked throws correctly`() {
    assertFailure { throwLocked { "Message" } }
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.LOCKED)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwNotFound throws correctly`() {
    assertFailure { throwNotFound { "Message" } }
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.NOT_FOUND)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwPreconditionFailed throws correctly`() {
    assertFailure { throwPreconditionFailed { "Message" } }
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwNotVerified throws correctly`() {
    assertFailure { throwNotVerified { "Message" } }
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.UNAUTHORIZED)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `requireValid doesn't throw on valid data`() {
    val result = Normalizer.Result("something", isValid = true)

    val normalized = result.requireValid { "Test" }

    assertThat(normalized).isEqualTo(result.value)
  }

  @Test fun `requireValid throws on invalid data`() {
    val result = Normalizer.Result("something", isValid = false)

    assertFailure { result.requireValid { "Test" } }
      .all {
        hasClass(ResponseStatusException::class.java)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Test '${result.value}' is invalid")
      }
  }

  @Test fun `silent wrapper succeeds if action does not throw`() {
    assertThat(silent { 1 + 1 })
      .isEqualTo(2)
  }

  @Test fun `silent wrapper ignores the error if action throws`() {
    assertThat(silent { error("fail") })
      .isNull()
  }

}
