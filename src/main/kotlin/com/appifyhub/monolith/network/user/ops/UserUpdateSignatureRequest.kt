package com.appifyhub.monolith.network.user.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class UserUpdateSignatureRequest(
  @JsonProperty("raw_signature_old") val rawSignatureOld: String,
  @JsonProperty("raw_signature_new") val rawSignatureNew: String,
) : Serializable
