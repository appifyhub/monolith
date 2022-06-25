package com.appifyhub.monolith.network.integrations

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class MailgunConfigDto(
  @JsonProperty("api_key") val apiKey: String,
  @JsonProperty("domain") val domain: String,
  @JsonProperty("sender_name") val senderName: String,
  @JsonProperty("sender_email") val senderEmail: String,
)
