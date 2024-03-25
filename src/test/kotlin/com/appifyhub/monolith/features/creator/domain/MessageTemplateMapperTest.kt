package com.appifyhub.monolith.features.creator.domain

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplateUpdater
import com.appifyhub.monolith.features.creator.storage.model.MessageTemplateDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.Test
import java.util.Date

class MessageTemplateMapperTest {

  @Test fun `message template data to domain`() {
    assertThat(Stubs.messageTemplateDbm.toDomain())
      .isDataClassEqualTo(Stubs.messageTemplate)
  }

  @Test fun `message template domain to data`() {
    assertThat(Stubs.messageTemplate.toData())
      .isEqualTo(Stubs.messageTemplateDbm)
  }

  @Test fun `message template creator domain to data`() {
    val timeProvider = TimeProviderFake(staticTime = { 0x100000L })

    assertThat(Stubs.messageTemplateCreator.toData(timeProvider))
      .isEqualTo(
        MessageTemplateDbm(
          id = null,
          project = Stubs.messageTemplateDbm.project,
          name = Stubs.messageTemplateDbm.name,
          languageTag = Stubs.messageTemplateDbm.languageTag,
          title = Stubs.messageTemplateDbm.title,
          content = Stubs.messageTemplateDbm.content,
          isHtml = Stubs.messageTemplateDbm.isHtml,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @Test fun `message template updater to message template (no changes)`() {
    val messageTemplateUpdater = MessageTemplateUpdater(
      id = Stubs.messageTemplate.id,
    )

    val result = messageTemplateUpdater.applyTo(
      template = Stubs.messageTemplate,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.messageTemplate.copy(
        updatedAt = Date(0),
      )
    )
  }

  @Test fun `message template updater to message template (with changes)`() {
    val result = Stubs.messageTemplateUpdater.applyTo(
      template = Stubs.messageTemplate,
      timeProvider = TimeProviderFake(),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.messageTemplateUpdated.copy(
        updatedAt = Date(0),
      )
    )
  }

}
