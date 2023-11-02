package com.appifyhub.monolith.service.integrations.push

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class LogPushSenderTest {

  private val sender = LogPushSender()

  @Test fun `sender has correct type`() {
    assertThat(sender.type)
      .isEqualTo(PushSender.Type.LOG)
  }

  @Test fun `sender logs to history`() {
    sender.send(
      project = Stubs.project.copy(id = 1),
      receiverToken = "token1",
      notification = PushSender.Notification(
        title = "title1",
        body = "body1",
        imageUrl = "url1",
      ),
      data = mapOf("k1" to "v1"),
    )
    sender.send(
      project = Stubs.project.copy(id = 2),
      receiverToken = "token2",
      notification = PushSender.Notification(
        title = "title2",
        body = "body2",
        imageUrl = "url2",
      ),
      data = mapOf("k2" to "v2"),
    )

    assertThat(sender)
      .all {
        transform { it.history }.hasSize(2)
        transform { it.history.first() }.isDataClassEqualTo(
          LogPushSender.SentPush(1, "token1", "title1", "body1", "url1", mapOf("k1" to "v1")),
        )
        transform { it.history[1] }.isDataClassEqualTo(
          LogPushSender.SentPush(2, "token2", "title2", "body2", "url2", mapOf("k2" to "v2")),
        )
      }
  }

}
