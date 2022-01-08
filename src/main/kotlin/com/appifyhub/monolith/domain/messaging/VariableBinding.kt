package com.appifyhub.monolith.domain.messaging

import java.util.Date

data class VariableBinding(
  val variableName: String,
  val bindsTo: TemplateDataBinder.Code,
  val createdAt: Date,
  val updatedAt: Date,
)
