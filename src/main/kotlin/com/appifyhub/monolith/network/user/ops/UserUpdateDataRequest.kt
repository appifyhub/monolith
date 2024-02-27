package com.appifyhub.monolith.network.user.ops

import com.appifyhub.monolith.network.common.SettableRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class UserUpdateDataRequest(
  @JsonProperty("name") val name: SettableRequest<String?>? = null,
  @JsonProperty("type") val type: SettableRequest<String>? = null,
  @JsonProperty("allows_spam") val allowsSpam: SettableRequest<Boolean>? = null,
  @JsonProperty("contact") val contact: SettableRequest<String?>? = null,
  @JsonProperty("contact_type") val contactType: SettableRequest<String>? = null,
  @JsonProperty("birthday") val birthday: SettableRequest<String?>? = null,
  @JsonProperty("company") val company: SettableRequest<OrganizationUpdaterDto?>? = null,
  @JsonProperty("language_tag") val languageTag: SettableRequest<String?>? = null,
) : Serializable
