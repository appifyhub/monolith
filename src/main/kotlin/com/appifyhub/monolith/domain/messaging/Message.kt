package com.appifyhub.monolith.domain.messaging

data class Message(
  val template: MessageTemplate,
  val materialized: String,
)
