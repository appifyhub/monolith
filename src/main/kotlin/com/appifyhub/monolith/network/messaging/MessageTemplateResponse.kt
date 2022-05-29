package com.appifyhub.monolith.network.messaging

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class MessageTemplateResponse(
  @JsonProperty("id") val id: Long,
  @JsonProperty("name") val name: String,
  @JsonProperty("language_tag") val languageTag: String,
  @JsonProperty("content") val content: String,
  @JsonProperty("is_html") val isHtml: Boolean,
  @JsonProperty("created_at") val createdAt: String,
  @JsonProperty("updated_at") val updatedAt: String,
) : Serializable
