package com.appifyhub.monolith.service.integrations.push

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.service.integrations.limitedDeque
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogPushSender : PushSender {

  data class SentPush(
    val projectId: Long,
    val receiverToken: String,
    val title: String?,
    val body: String?,
    val imageUrl: String?,
    val data: Map<String, String>?,
  )

  private val log = LoggerFactory.getLogger(this::class.java)

  override val type = PushSender.Type.LOG

  val history: ArrayDeque<SentPush> by limitedDeque(limit = 50)

  override fun send(
    project: Project,
    receiverToken: String,
    notification: PushSender.Notification?,
    data: Map<String, String>?,
  ) = SentPush(project.id, receiverToken, notification?.title, notification?.body, notification?.imageUrl, data).let {
    history += it
    log.info("Push sent: $it")
  }

}
