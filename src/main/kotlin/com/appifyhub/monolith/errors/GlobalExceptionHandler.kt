package com.appifyhub.monolith.errors

import com.appifyhub.monolith.network.common.MessageResponse
import com.fasterxml.jackson.databind.ObjectMapper
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler(
  private val jsonMapper: ObjectMapper,
) : ResponseEntityExceptionHandler(), AuthenticationEntryPoint {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun commence(
    request: HttpServletRequest,
    response: HttpServletResponse,
    authException: AuthenticationException,
  ) {
    val entity = handleThrowable(authException)
    val entityJson = jsonMapper.writeValueAsString(entity.body)
    response.status = entity.statusCodeValue
    response.contentType = MediaType.APPLICATION_JSON_VALUE
    response.outputStream.println(entityJson)
  }

  @ExceptionHandler(Throwable::class)
  fun handleThrowable(t: Throwable): ResponseEntity<MessageResponse> =
    when {

      t is AuthenticationException ||
        t is AccessDeniedException ||
        t.message?.contains("access is denied", ignoreCase = true) == true ->

        ResponseEntity(
          MessageResponse("Unauthorized : ${t.message}"),
          HttpHeaders(),
          HttpStatus.UNAUTHORIZED,
        )

      t.message?.contains("invalid credentials", ignoreCase = true) == true ->
        ResponseEntity(
          MessageResponse("Credentials Error : ${t.message}"),
          HttpHeaders(),
          HttpStatus.UNAUTHORIZED,
        )

      t.message?.contains("token is blocked", ignoreCase = true) == true ->
        ResponseEntity(
          MessageResponse("Access Error : ${t.message}"),
          HttpHeaders(),
          HttpStatus.UNAUTHORIZED,
        )

      t.message?.contains("token expired", ignoreCase = true) == true ->
        ResponseEntity(
          MessageResponse("Access Error : ${t.message}"),
          HttpHeaders(),
          HttpStatus.UNAUTHORIZED,
        )

      t is ResponseStatusException ->
        ResponseEntity(
          MessageResponse(t.reason ?: "Request Error : ${t.message}"),
          HttpHeaders(),
          t.status,
        )

      t is IllegalStateException ->
        ResponseEntity(
          MessageResponse("Request Error : ${t.message}"),
          HttpHeaders(),
          HttpStatus.UNPROCESSABLE_ENTITY,
        )

      t is IllegalArgumentException ->
        ResponseEntity(
          MessageResponse("Request Error : ${t.message}"),
          HttpHeaders(),
          HttpStatus.UNPROCESSABLE_ENTITY,
        )

      t is NoSuchElementException ->
        ResponseEntity(
          MessageResponse("Request Error : ${t.message}"),
          HttpHeaders(),
          HttpStatus.NOT_FOUND,
        )

      t is EmptyResultDataAccessException ->
        ResponseEntity(
          MessageResponse("Request Error : ${t.message}"),
          HttpHeaders(),
          HttpStatus.NOT_FOUND,
        )

      else ->
        ResponseEntity(
          MessageResponse("Internal Error : ${t.message}"),
          HttpHeaders(),
          HttpStatus.INTERNAL_SERVER_ERROR,
        )

    }.also {
      log.error("Responding with ${it.statusCodeValue}/${it.statusCode.name}", t)
    }

}
