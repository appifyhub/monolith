package com.appifyhub.monolith.service.integrations.push

import com.appifyhub.monolith.domain.creator.Project

interface PushSender {

  enum class Type {
    FIREBASE,
    LOG,
  }

  data class Notification(
    val title: String,
    val body: String?,
    val imageUrl: String?,
  )

  val type: Type

  fun send(
    project: Project,
    receiverToken: String,
    notification: Notification? = null,
    data: Map<String, String>? = null,
  )

}
