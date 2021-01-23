package com.appifyhub.monolith.config

import com.appifyhub.monolith.network.common.MessageResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {

  private val log = LoggerFactory.getLogger(this::class.java)

  @ExceptionHandler(Throwable::class)
  fun handleThrowable(t: Throwable) = when {

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