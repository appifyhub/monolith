package com.appifyhub.monolith.errors

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.appifyhub.monolith.network.common.MessageResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.Answers.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.server.ResponseStatusException

class GlobalExceptionHandlerTest {

  private val testMapper = jacksonObjectMapper()
  private val handler = GlobalExceptionHandler(testMapper)

  @Test fun `handle pre-auth failure with commence`() {
    val request = mock<HttpServletRequest>()
    val response = mock<HttpServletResponse>(defaultAnswer = RETURNS_DEEP_STUBS)
    val exception: AuthenticationException = InsufficientAuthenticationException("Something failed")

    handler.commence(request, response, exception)

    verifyNoMoreInteractions(request)
    verify(response).status = HttpStatus.UNAUTHORIZED.value()
    verify(response).contentType = MediaType.APPLICATION_JSON_VALUE
    verify(response.outputStream).println(
      testMapper.writeValueAsString(
        MessageResponse(message = "Unauthorized : ${exception.message}")
      )
    )
  }

  @Test fun `handle authentication exception`() {
    val exception: AuthenticationException = InsufficientAuthenticationException("Something failed")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNAUTHORIZED)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Unauthorized : ${exception.message}")
    }
  }

  @Test fun `handle access denied exception`() {
    val exception = AccessDeniedException("Something failed")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNAUTHORIZED)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Unauthorized : ${exception.message}")
    }
  }

  @Test fun `handle credentials invalid message`() {
    val exception = Throwable("Invalid credentials")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.UNAUTHORIZED)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Credentials Error : ${exception.message}")
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
        .isEqualTo("Unauthorized : ${exception.message}")
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
        .isEqualTo("Access Error : ${exception.message}")
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
        .isEqualTo("Access Error : ${exception.message}")
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

  @Test fun `handle no such element exception`() {
    val exception = NoSuchElementException("Something failed")

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.NOT_FOUND)
      prop("headers") { it.headers }
        .isEqualTo(HttpHeaders())
      prop("message") { it.body?.message }
        .isEqualTo("Request Error : ${exception.message}")
    }
  }

  @Test fun `handle empty result data access exception`() {
    val exception = EmptyResultDataAccessException("Not found", 1)

    val result = handler.handleThrowable(exception)

    assertThat(result).all {
      prop("status") { it.statusCode }
        .isEqualTo(HttpStatus.NOT_FOUND)
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
        .isEqualTo("Internal Error : ${exception.message}")
    }
  }

}
