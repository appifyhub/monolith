package com.appifyhub.monolith.network.creator.project.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class ProjectCreateRequest(
  @JsonProperty("type") val type: String,
  @JsonProperty("user_id_type") val userIdType: String,
  @JsonProperty("owner_universal_id") val ownerUniversalId: String,
  @JsonProperty("name") val name: String,
  @JsonProperty("description") val description: String? = null,
  @JsonProperty("logo_url") val logoUrl: String? = null,
  @JsonProperty("website_url") val websiteUrl: String? = null,
  @JsonProperty("language_tag") val languageTag: String?,
)
