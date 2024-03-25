package com.appifyhub.monolith.features.creator.integrations.sms

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class LogSmsSenderTest {

  private val sender = LogSmsSender()

  @Test fun `sender has correct type`() {
    assertThat(sender.type)
      .isEqualTo(SmsSender.Type.LOG)
  }

  @Test fun `sender logs to history`() {
    sender.send(Stubs.project.copy(id = 1), "+491760000000", "body1")
    sender.send(Stubs.project.copy(id = 2), "+491760000001", "body2")

    assertThat(sender)
      .all {
        transform { it.history }.hasSize(2)
        transform { it.history.first() }.isDataClassEqualTo(
          LogSmsSender.SentSms(1, "+491760000000", "body1"),
        )
        transform { it.history[1] }.isDataClassEqualTo(
          LogSmsSender.SentSms(2, "+491760000001", "body2"),
        )
      }
  }

}
