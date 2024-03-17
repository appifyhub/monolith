package com.appifyhub.monolith.network.creator.messaging.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class MessageTemplateCreateRequest(
  @JsonProperty("name") val name: String,
  @JsonProperty("language_tag") val languageTag: String,
  @JsonProperty("title") val title: String,
  @JsonProperty("content") val content: String,
  @JsonProperty("is_html") val isHtml: Boolean,
) : Serializable
