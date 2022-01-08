package com.appifyhub.monolith.domain.messaging

import java.util.Date

data class MessageTemplate(
  val id: Long,
  val projectId: Long,
  val name: String,
  val language: String,
  val content: String,
  val isHtml: Boolean,
  val bindings: List<VariableBinding>,
  val createdAt: Date,
  val updatedAt: Date,
)
