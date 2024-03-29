package com.appifyhub.monolith.features.creator.domain.model.messaging

import com.appifyhub.monolith.util.extension.takeIfNotBlank

data class TwilioConfig(
  val accountSid: String,
  val authToken: String,
  val messagingServiceId: String,
  val maxPricePerMessage: Int, // USD
  val maxRetryAttempts: Int,
  val defaultSenderName: String, // set to blank to clear
  val defaultSenderNumber: String,
) {
  val userAuth: String = "$accountSid:$authToken"
  val defaultSender: String = defaultSenderName.takeIfNotBlank() ?: defaultSenderNumber
}
