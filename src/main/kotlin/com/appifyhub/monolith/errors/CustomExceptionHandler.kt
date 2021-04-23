package com.appifyhub.monolith.errors

import com.appifyhub.monolith.network.common.MessageResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {

  private val log = LoggerFactory.getLogger(this::class.java)

  @ExceptionHandler(Throwable::class)
  fun handleThrowable(t: Throwable): ResponseEntity<MessageResponse> =
    when {

      t is AccessDeniedException || t.message?.toLowerCase()?.contains("access is denied") == true ->
        ResponseEntity(
          MessageResponse("Unauthorized Access : ${t.message}"),
          HttpHeaders(),
          HttpStatus.UNAUTHORIZED,
        )

      t.message?.toLowerCase()?.contains("token is blocked") == true ->
        ResponseEntity(
          MessageResponse("Blocked Access : ${t.message}"),
          HttpHeaders(),
          HttpStatus.UNAUTHORIZED,
        )

      t.message?.toLowerCase()?.contains("token expired") == true ->
        ResponseEntity(
          MessageResponse("Expired Access : ${t.message}"),
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

      else ->
        ResponseEntity(
          MessageResponse("Internal Failure : ${t.message}"),
          HttpHeaders(),
          HttpStatus.INTERNAL_SERVER_ERROR,
        )

    }.also {
      log.error("Responding with ${it.statusCodeValue}/${it.statusCode.name}", t)
    }

}
