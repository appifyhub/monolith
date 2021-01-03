package com.appifyhub.monolith.controller.auth

import com.appifyhub.monolith.controller.common.Headers
import com.appifyhub.monolith.network.auth.AdminCredentialsRequest
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.auth.UserCredentialsRequest
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.util.unauthorized
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
  private val authService: AuthService,
) {

  object Endpoints {
    const val AUTH_USERS = "/v1/auth"
    const val AUTH_ADMINS = "/v1/admin/auth"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.AUTH_USERS)
  fun authUsers(
    @RequestBody creds: UserCredentialsRequest,
    @RequestHeader(Headers.PROJECT_SIGNATURE) projectSignature: String,
  ): TokenResponse {
    val user = try {
      authService.fetchUserByCredentials(projectSignature, creds.identifier, creds.secret)
    } catch (t: Throwable) {
      log.warn("Failed to find user identified by ${creds.identifier}", t)
      unauthorized("Invalid credentials")
    }

    val token = authService.generateTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

  @PostMapping(Endpoints.AUTH_ADMINS)
  fun authAdmins(
    @RequestBody creds: AdminCredentialsRequest,
  ): TokenResponse {
    val user = try {
      authService.fetchAdminUserByCredentials(creds.identifier, creds.secret)
    } catch (t: Throwable) {
      log.warn("Failed to find admin identified by ${creds.identifier}", t)
      unauthorized("Invalid credentials")
    }

    val token = authService.generateTokenFor(user, creds.origin)
    return TokenResponse(token)
  }

}