package com.appifyhub.monolith.features.creator.api.model

import com.appifyhub.monolith.features.creator.api.model.messaging.FirebaseConfigDto
import com.appifyhub.monolith.features.creator.api.model.messaging.MailgunConfigDto
import com.appifyhub.monolith.features.creator.api.model.messaging.TwilioConfigDto
import com.appifyhub.monolith.features.common.api.model.SettableRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ProjectUpdateRequest(
  @JsonProperty("type") val type: SettableRequest<String>? = null,
  @JsonProperty("status") val status: SettableRequest<String>? = null,
  @JsonProperty("name") val name: SettableRequest<String>? = null,
  @JsonProperty("description") val description: SettableRequest<String?>? = null,
  @JsonProperty("logo_url") val logoUrl: SettableRequest<String?>? = null,
  @JsonProperty("website_url") val websiteUrl: SettableRequest<String?>? = null,
  @JsonProperty("max_users") val maxUsers: SettableRequest<Int>? = null,
  @JsonProperty("anyone_can_search") val anyoneCanSearch: SettableRequest<Boolean>? = null,
  @JsonProperty("on_hold") val onHold: SettableRequest<Boolean>? = null,
  @JsonProperty("language_tag") val languageTag: SettableRequest<String?>? = null,
  @JsonProperty("requires_signup_codes") val requiresSignupCodes: SettableRequest<Boolean>? = null,
  @JsonProperty("max_signup_codes_per_user") val maxSignupCodesPerUser: SettableRequest<Int>? = null,
  @JsonProperty("mailgun_config") val mailgunConfig: SettableRequest<MailgunConfigDto?>? = null,
  @JsonProperty("twilio_config") val twilioConfig: SettableRequest<TwilioConfigDto?>? = null,
  @JsonProperty("firebase_config") val firebaseConfig: SettableRequest<FirebaseConfigDto?>? = null,
) : Serializable
