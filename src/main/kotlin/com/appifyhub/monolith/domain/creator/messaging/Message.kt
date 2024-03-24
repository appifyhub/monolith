package com.appifyhub.monolith.domain.creator.messaging

data class Message(
  val template: MessageTemplate,
  val materialized: String,
)
