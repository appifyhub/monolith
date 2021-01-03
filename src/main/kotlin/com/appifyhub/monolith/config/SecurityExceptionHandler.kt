package com.appifyhub.monolith.config

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class SecurityExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(
    exception: Exception,
    request: WebRequest?,
  ) = ResponseEntity("Unauthorized Access", HttpHeaders(), HttpStatus.UNAUTHORIZED)

}