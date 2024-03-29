package com.appifyhub.monolith.features.creator.api.model.messaging

import com.appifyhub.monolith.features.common.api.model.SettableRequest
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class MessageTemplateUpdateRequest(
  @JsonProperty("name") val name: SettableRequest<String>? = null,
  @JsonProperty("language_tag") val languageTag: SettableRequest<String>? = null,
  @JsonProperty("title") val title: SettableRequest<String>? = null,
  @JsonProperty("content") val content: SettableRequest<String>? = null,
  @JsonProperty("is_html") val isHtml: SettableRequest<Boolean>? = null,
) : Serializable
