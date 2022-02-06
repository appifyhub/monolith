package com.appifyhub.monolith.util.ext

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import assertk.assertions.isSuccess
import assertk.assertions.prop
import com.appifyhub.monolith.validation.Normalizer
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ErrorExtensionsTest {

  @Test fun `throwUnauthorized throws correctly`() {
    assertThat { throwUnauthorized { "Message" } }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.UNAUTHORIZED)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwNormalization throws correctly`() {
    assertThat { throwNormalization { "Message" } }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwLocked throws correctly`() {
    assertThat { throwLocked { "Message" } }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.LOCKED)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwNotFound throws correctly`() {
    assertThat { throwNotFound { "Message" } }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.NOT_FOUND)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwPreconditionFailed throws correctly`() {
    assertThat { throwPreconditionFailed { "Message" } }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Message")
      }
  }

  @Test fun `throwNotVerified throws correctly`() {
    assertThat { throwNotVerified { "Message" } }
      .isFailure()
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

    assertThat { result.requireValid { "Test" } }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class.java)
        prop("status") { (it as ResponseStatusException).status }
          .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
        prop("reason") { (it as ResponseStatusException).reason }
          .isEqualTo("Test '${result.value}' is invalid")
      }
  }

  @Test fun `silent wrapper succeeds if action does not throw`() {
    assertThat {
      silent { 1 + 1 }
    }
      .isSuccess()
      .isEqualTo(2)
  }

  @Test fun `silent wrapper ignores the error if action throws`() {
    assertThat {
      silent { error("fail") }
    }
      .isSuccess()
      .isNull()
  }

}
