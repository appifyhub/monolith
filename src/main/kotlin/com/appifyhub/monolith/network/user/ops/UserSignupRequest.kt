package com.appifyhub.monolith.network.user.ops

import com.appifyhub.monolith.network.user.OrganizationDto
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class UserSignupRequest(
  @JsonProperty("user_id") val userId: String? = null,
  @JsonProperty("raw_signature") val rawSignature: String,
  @JsonProperty("name") val name: String? = null,
  @JsonProperty("type") val type: String? = null,
  @JsonProperty("allows_spam") val allowsSpam: Boolean?,
  @JsonProperty("contact") val contact: String? = null,
  @JsonProperty("contact_type") val contactType: String? = null,
  @JsonProperty("birthday") val birthday: String? = null,
  @JsonProperty("company") val company: OrganizationDto?,
  @JsonProperty("language_tag") val languageTag: String? = null,
) : Serializable
