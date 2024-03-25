package com.appifyhub.monolith.features.creator.domain.model.messaging

import com.appifyhub.monolith.domain.common.Settable

data class MessageTemplateUpdater(
  val id: Long,
  val name: Settable<String>? = null,
  val languageTag: Settable<String>? = null,
  val title: Settable<String>? = null,
  val content: Settable<String>? = null,
  val isHtml: Settable<Boolean>? = null,
)
