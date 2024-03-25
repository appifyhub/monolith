package com.appifyhub.monolith.features.creator.integrations.sms

import com.appifyhub.monolith.features.creator.domain.model.Project

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
