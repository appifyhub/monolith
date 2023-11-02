package com.appifyhub.monolith.domain.integrations

data class MailgunConfig(
  val apiKey: String,
  val domain: String,
  val senderName: String,
  val senderEmail: String,
)
