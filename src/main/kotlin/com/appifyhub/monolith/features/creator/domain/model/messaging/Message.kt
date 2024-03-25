package com.appifyhub.monolith.features.creator.domain.model.messaging

data class Message(
  val template: MessageTemplate,
  val materialized: String,
)
