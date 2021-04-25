package com.appifyhub.monolith.network.user.ops

import com.appifyhub.monolith.network.common.SettableRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class UserUpdaterRequest(
  // cannot be set to null (can be skipped)
  @JsonProperty("raw_signature") val rawSignature: SettableRequest<String>?,
  @JsonProperty("type") val type: SettableRequest<String>?,
  @JsonProperty("authority") val authority: SettableRequest<String>?,
  @JsonProperty("contact_type") val contactType: SettableRequest<String>?,
  @JsonProperty("allows_spam") val allowsSpam: SettableRequest<Boolean>?,

  // can be set to null (and also skipped)
  @JsonProperty("name") val name: SettableRequest<String?>?,
  @JsonProperty("contact") val contact: SettableRequest<String?>?,
  @JsonProperty("birthday") val birthday: SettableRequest<String?>?,
  @JsonProperty("company") val company: SettableRequest<OrganizationUpdaterDto?>?,
) : Serializable