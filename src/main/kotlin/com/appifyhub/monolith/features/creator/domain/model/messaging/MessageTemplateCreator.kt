package com.appifyhub.monolith.features.creator.domain.model.messaging

data class MessageTemplateCreator(
  val projectId: Long,
  val name: String,
  val languageTag: String,
  val title: String,
  val content: String,
  val isHtml: Boolean,
)
