package com.appifyhub.monolith.network.messaging.ops

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonInclude(Include.NON_NULL)
data class MessageTemplateCreateRequest(
  @JsonProperty("name") val name: String,
  @JsonProperty("languageTag") val languageTag: String,
  @JsonProperty("content") val content: String,
  @JsonProperty("isHtml") val isHtml: Boolean,
) : Serializable
