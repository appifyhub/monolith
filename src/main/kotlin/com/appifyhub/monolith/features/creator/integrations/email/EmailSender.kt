package com.appifyhub.monolith.features.creator.integrations.email

import com.appifyhub.monolith.features.creator.domain.model.Project

interface EmailSender {

  enum class Type {
    MAILGUN,
    LOG,
  }

  val type: Type

  fun send(
    project: Project,
    toEmail: String,
    title: String,
    body: String,
    isHtml: Boolean,
  )

}
