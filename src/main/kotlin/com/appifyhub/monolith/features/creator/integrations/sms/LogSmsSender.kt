package com.appifyhub.monolith.features.creator.integrations.sms

import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.integrations.limitedDeque
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogSmsSender : SmsSender {

  data class SentSms(
    val projectId: Long,
    val toNumber: String,
    val body: String,
  )

  private val log = LoggerFactory.getLogger(this::class.java)

  override val type = SmsSender.Type.LOG

  val history: ArrayDeque<SentSms> by limitedDeque(limit = 50)

  override fun send(
    project: Project,
    toNumber: String,
    body: String,
  ) = SentSms(project.id, toNumber, body).let {
    history += it
    log.info("SMS sent: $it")
  }

}
