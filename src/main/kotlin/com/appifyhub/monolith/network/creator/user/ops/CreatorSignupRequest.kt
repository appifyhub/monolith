package com.appifyhub.monolith.network.creator.user.ops

import com.appifyhub.monolith.network.user.OrganizationDto
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class CreatorSignupRequest(
  @JsonProperty("user_id") val userId: String,
  @JsonProperty("raw_signature") val rawSignature: String,
  @JsonProperty("name") val name: String,
  @JsonProperty("type") val type: String,
  @JsonProperty("birthday") val birthday: String? = null,
  @JsonProperty("company") val company: OrganizationDto?,
) : Serializable
