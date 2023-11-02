package com.appifyhub.monolith.service.integrations.sms

import com.appifyhub.monolith.domain.creator.Project

interface SmsSender {

  enum class Type {
    TWILIO,
    LOG,
  }

  val type: Type

  fun send(
    project: Project,
    toNumber: String,
    body: String,
  )

}
