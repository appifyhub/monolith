package com.appifyhub.monolith.features.creator.api.model.messaging

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(Include.NON_NULL)
data class TwilioConfigDto(
  @JsonProperty("account_sid") val accountSid: String,
  @JsonProperty("auth_token") val authToken: String,
  @JsonProperty("messaging_service_id") val messagingServiceId: String,
  @JsonProperty("max_price_per_message") val maxPricePerMessage: Int, // USD
  @JsonProperty("max_retry_attempts") val maxRetryAttempts: Int,
  @JsonProperty("default_sender_name") val defaultSenderName: String, // set to blank to clear
  @JsonProperty("default_sender_number") val defaultSenderNumber: String,
)
