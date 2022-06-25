package com.appifyhub.monolith.service.integrations.email

import com.appifyhub.monolith.domain.creator.Project
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LogEmailSender : EmailSender {

  data class SentEmail(
    val projectId: Long,
    val toEmail: String,
    val title: String,
    val body: String,
    val isHtml: Boolean,
  )

  private val log = LoggerFactory.getLogger(this::class.java)

  override val type = EmailSender.Type.LOG

  val history = mutableListOf<SentEmail>()

  override fun send(
    project: Project,
    toEmail: String,
    title: String,
    body: String,
    isHtml: Boolean,
  ) = SentEmail(project.id, toEmail, title, body, isHtml).let {
    history += it
    log.info("Email sent: $it")
  }

}
