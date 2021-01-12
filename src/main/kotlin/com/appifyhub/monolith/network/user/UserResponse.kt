package com.appifyhub.monolith.network.user

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class UserResponse(
  @JsonProperty("user_id") val userId: String,
  @JsonProperty("project_id") val projectId: Long,
  @JsonProperty("name") val name: String?,
  @JsonProperty("type") val type: String,
  @JsonProperty("authority") val authority: String,
  @JsonProperty("allows_spam") val allowsSpam: Boolean,
  @JsonProperty("contact") val contact: String?,
  @JsonProperty("contact_type") val contactType: String,
  @JsonProperty("birthday") val birthday: String?,
  @JsonProperty("company") val company: OrganizationDto?,
  @JsonProperty("created_at") val createdAt: String,
  @JsonProperty("updated_at") val updatedAt: String,
) : Serializable