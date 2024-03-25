package com.appifyhub.monolith.features.user.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class SignupCodesResponse(
  @JsonProperty("signup_codes") val signupCodes: List<SignupCodeResponse>,
  @JsonProperty("max_signup_codes") val maxSignupCodes: Int,
) : Serializable
