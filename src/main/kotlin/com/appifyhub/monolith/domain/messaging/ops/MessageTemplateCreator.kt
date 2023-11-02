package com.appifyhub.monolith.domain.messaging.ops

data class MessageTemplateCreator(
  val projectId: Long,
  val name: String,
  val languageTag: String,
  val title: String,
  val content: String,
  val isHtml: Boolean,
)
