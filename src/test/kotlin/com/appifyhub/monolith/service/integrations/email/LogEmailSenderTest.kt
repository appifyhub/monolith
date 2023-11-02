package com.appifyhub.monolith.service.integrations.email

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class LogEmailSenderTest {

  private val sender = LogEmailSender()

  @Test fun `sender has correct type`() {
    assertThat(sender.type)
      .isEqualTo(EmailSender.Type.LOG)
  }

  @Test fun `sender logs to history`() {
    sender.send(Stubs.project.copy(id = 1), "email1", "title1", "body1", false)
    sender.send(Stubs.project.copy(id = 2), "email2", "title2", "body2", true)

    assertThat(sender)
      .all {
        transform { it.history }.hasSize(2)
        transform { it.history.first() }.isDataClassEqualTo(
          LogEmailSender.SentEmail(1, "email1", "title1", "body1", false)
        )
        transform { it.history[1] }.isDataClassEqualTo(
          LogEmailSender.SentEmail(2, "email2", "title2", "body2", true)
        )
      }
  }

}
