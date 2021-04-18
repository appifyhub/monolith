package com.appifyhub.monolith.errors

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.server.ResponseStatusException

class CustomExceptionHandlerTest {

  private val handler = CustomExceptionHandler()

  @Test fun `handle access denied exception`() {
    val exception = AccessDeniedException("something")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNAUTHORIZED)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Unauthorized Access : ${exception.message}")
    }
  }

  @Test fun `handle access denied message`() {
    val exception = Throwable("Access is denied")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNAUTHORIZED)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Unauthorized Access : ${exception.message}")
    }
  }

  @Test fun `handle blocked token message`() {
    val exception = Throwable("Token is blocked")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNAUTHORIZED)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Blocked Access : ${exception.message}")
    }
  }

  @Test fun `handle expired token message`() {
    val exception = Throwable("Token expired")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNAUTHORIZED)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Expired Access : ${exception.message}")
    }
  }

  @Test fun `handle response status exception with reason`() {
    val exception = ResponseStatusException(HttpStatus.MULTI_STATUS, "Reason")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.MULTI_STATUS)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo(exception.reason)
    }
  }

  @Test fun `handle response status exception without reason`() {
    val exception = ResponseStatusException(HttpStatus.MULTI_STATUS)

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.MULTI_STATUS)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Request Error : ${exception.message}")
    }
  }

  @Test fun `handle illegal state exception`() {
    val exception = IllegalStateException("Something failed")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Request Error : ${exception.message}")
    }
  }

  @Test fun `handle illegal argument exception`() {
    val exception = IllegalArgumentException("Something failed")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Request Error : ${exception.message}")
    }
  }

  @Test fun `handle random exception`() {
    val exception = Throwable("Random")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Internal Failure : ${exception.message}")
    }
  }

}
