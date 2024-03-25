package com.appifyhub.monolith.features.user.api

import com.appifyhub.monolith.features.common.api.Endpoints
import com.appifyhub.monolith.features.auth.domain.access.AccessManager
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Privilege
import com.appifyhub.monolith.features.user.api.model.SignupCodeResponse
import com.appifyhub.monolith.features.user.api.model.SignupCodesResponse
import com.appifyhub.monolith.features.user.domain.model.SignupCode
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.service.SignupCodeService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SignupCodeController(
  private val signupCodeService: SignupCodeService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.UNIVERSAL_USER_SIGNUP_CODES)
  fun createSignupCode(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): SignupCodeResponse {
    log.debug("[POST] creating signup code for user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)
    accessManager.requireProjectFeaturesFunctional(userId.projectId, Feature.USERS)
    val owner = accessManager.requestUserAccess(authentication, userId, Privilege.USER_WRITE_SIGNUP_CODE)

    return signupCodeService.createCode(owner.id).toNetwork()
  }

  @GetMapping(Endpoints.UNIVERSAL_USER_SIGNUP_CODES)
  fun fetchAllSignupCodesForUser(
    authentication: Authentication,
    @PathVariable universalId: String,
  ): SignupCodesResponse {
    log.debug("[GET] fetching all signup codes for user $universalId")

    val userId = UserId.fromUniversalFormat(universalId)
    accessManager.requireProjectFunctional(userId.projectId)
    accessManager.requireProjectFeaturesFunctional(userId.projectId, Feature.USERS)
    val owner = accessManager.requestUserAccess(authentication, userId, Privilege.USER_READ_SIGNUP_CODE)
    val project = accessManager.requestProjectAccess(authentication, userId.projectId, Privilege.PROJECT_READ_BASIC)
    val maxSignupCodes = project.maxSignupCodesPerUser
    val allSignupCodes = signupCodeService.fetchAllCodesByOwner(owner.id)

    return SignupCodesResponse(
      signupCodes = allSignupCodes.map(SignupCode::toNetwork),
      maxSignupCodes = maxSignupCodes,
    )
  }

}
