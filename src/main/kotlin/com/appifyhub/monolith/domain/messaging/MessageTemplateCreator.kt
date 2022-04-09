package com.appifyhub.monolith.domain.messaging

data class MessageTemplateCreator(
  val projectId: Long,
  val name: String,
  val languageTag: String,
  val content: String,
  val isHtml: Boolean,
)
