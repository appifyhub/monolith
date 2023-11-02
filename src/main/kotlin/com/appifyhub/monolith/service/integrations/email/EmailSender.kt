package com.appifyhub.monolith.service.integrations.email

import com.appifyhub.monolith.domain.creator.Project

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
