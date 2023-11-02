package com.appifyhub.monolith.network.creator.project

import com.appifyhub.monolith.network.integrations.FirebaseConfigDto
import com.appifyhub.monolith.network.integrations.MailgunConfigDto
import com.appifyhub.monolith.network.integrations.TwilioConfigDto
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class ProjectResponse(
  @JsonProperty("project_id") val projectId: Long,
  @JsonProperty("type") val type: String,
  @JsonProperty("state") val state: ProjectStateResponse,
  @JsonProperty("user_id_type") val userIdType: String,
  @JsonProperty("name") val name: String,
  @JsonProperty("description") val description: String?,
  @JsonProperty("logo_url") val logoUrl: String?,
  @JsonProperty("website_url") val websiteUrl: String?,
  @JsonProperty("max_users") val maxUsers: Int,
  @JsonProperty("anyone_can_search") val anyoneCanSearch: Boolean,
  @JsonProperty("on_hold") val onHold: Boolean,
  @JsonProperty("language_tag") val languageTag: String?,
  @JsonProperty("mailgun_config") val mailgunConfig: MailgunConfigDto?,
  @JsonProperty("twilio_config") val twilioConfig: TwilioConfigDto?,
  @JsonProperty("firebase_config") val firebaseConfig: FirebaseConfigDto?,
  @JsonProperty("created_at") val createdAt: String,
  @JsonProperty("updated_at") val updatedAt: String,
) : Serializable
