package com.appifyhub.monolith.features.creator.domain.model.messaging

data class MailgunConfig(
  val apiKey: String,
  val domain: String,
  val senderName: String,
  val senderEmail: String,
)
