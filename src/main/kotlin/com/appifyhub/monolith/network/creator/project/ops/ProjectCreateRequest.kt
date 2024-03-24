package com.appifyhub.monolith.network.creator.project.ops

import com.appifyhub.monolith.network.integrations.FirebaseConfigDto
import com.appifyhub.monolith.network.integrations.MailgunConfigDto
import com.appifyhub.monolith.network.integrations.TwilioConfigDto
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ProjectCreateRequest(
  @JsonProperty("type") val type: String,
  @JsonProperty("user_id_type") val userIdType: String,
  @JsonProperty("owner_universal_id") val ownerUniversalId: String,
  @JsonProperty("name") val name: String,
  @JsonProperty("description") val description: String? = null,
  @JsonProperty("logo_url") val logoUrl: String? = null,
  @JsonProperty("website_url") val websiteUrl: String? = null,
  @JsonProperty("language_tag") val languageTag: String? = null,
  @JsonProperty("max_users") val maxUsers: Int? = null,
  @JsonProperty("anyone_can_search") val anyoneCanSearch: Boolean? = null,
  @JsonProperty("requires_signup_codes") val requiresSignupCodes: Boolean? = null,
  @JsonProperty("max_signup_codes_per_user") val maxSignupCodesPerUser: Int? = null,
  @JsonProperty("mailgun_config") val mailgunConfig: MailgunConfigDto? = null,
  @JsonProperty("twilio_config") val twilioConfig: TwilioConfigDto? = null,
  @JsonProperty("firebase_config") val firebaseConfig: FirebaseConfigDto? = null,
) : Serializable
