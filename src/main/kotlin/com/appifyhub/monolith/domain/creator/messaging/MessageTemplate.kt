package com.appifyhub.monolith.domain.creator.messaging

import java.util.Date

data class MessageTemplate(
  val id: Long,
  val projectId: Long,
  val name: String,
  val languageTag: String,
  val title: String,
  val content: String,
  val isHtml: Boolean,
  val createdAt: Date,
  val updatedAt: Date,
)
