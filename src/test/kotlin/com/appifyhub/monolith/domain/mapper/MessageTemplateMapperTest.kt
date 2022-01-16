package com.appifyhub.monolith.domain.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.Test

class MessageTemplateMapperTest {

  @Test fun `messaging template data to domain`() {
    assertThat(Stubs.messageTemplateDbm.toDomain())
      .isDataClassEqualTo(Stubs.messageTemplate)
  }

  @Test fun `messaging template domain to data`() {
    assertThat(Stubs.messageTemplate.toData())
      .isEqualTo(Stubs.messageTemplateDbm)
  }

  @Test fun `messaging template creator domain to data`() {
    val timeProvider = TimeProviderFake(staticTime = { 0x100000L })

    assertThat(Stubs.messageTemplateCreator.toData(timeProvider))
      .isEqualTo(
        MessageTemplateDbm(
          id = null,
          project = Stubs.messageTemplateDbm.project,
          name = Stubs.messageTemplateDbm.name,
          language = Stubs.messageTemplateDbm.language,
          content = Stubs.messageTemplateDbm.content,
          isHtml = Stubs.messageTemplateDbm.isHtml,
          bindings = emptyList(),
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @Test fun `variable binding data to domain`() {
    assertThat(Stubs.variableBindingDbm.toDomain())
      .isDataClassEqualTo(Stubs.variableBinding)
  }

  @Test fun `variable binding domain to data`() {
    assertThat(Stubs.variableBinding.toData(Stubs.messageTemplate.id, Stubs.project.id))
      .isEqualTo(Stubs.variableBindingDbm)
  }

}
