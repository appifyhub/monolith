package com.appifyhub.monolith.network.user.ops

import com.appifyhub.monolith.network.user.OrganizationDto
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class UserCreatorRequest(
  @JsonProperty("user_id") val id: String?,
  @JsonProperty("raw_signature") val rawSignature: String,
  @JsonProperty("name") val name: String?,
  @JsonProperty("type") val type: String?,
  @JsonProperty("authority") val authority: String?,
  @JsonProperty("allows_spam") val allowsSpam: Boolean?,
  @JsonProperty("contact") val contact: String?,
  @JsonProperty("contact_type") val contactType: String?,
  @JsonProperty("birthday") val birthday: String?,
  @JsonProperty("company") val company: OrganizationDto?,
) : Serializable