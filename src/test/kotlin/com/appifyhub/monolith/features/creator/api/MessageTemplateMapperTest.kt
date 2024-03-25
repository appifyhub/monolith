package com.appifyhub.monolith.features.creator.api

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.features.creator.api.model.messaging.MessageInputsRequest
import com.appifyhub.monolith.features.creator.api.model.messaging.MessageTemplateUpdateRequest
import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplateUpdater
import com.appifyhub.monolith.features.creator.domain.model.messaging.Variable
import com.appifyhub.monolith.features.creator.domain.service.MessageTemplateService.Inputs
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class MessageTemplateMapperTest {

  @Test fun `variable domain to network`() {
    assertThat(Variable.USER_NAME.toNetwork())
      .isDataClassEqualTo(Stubs.variableResponse)
  }

  @Test fun `message template domain to network`() {
    assertThat(Stubs.messageTemplate.toNetwork())
      .isDataClassEqualTo(Stubs.messageTemplateResponse)
  }

  @Test fun `message domain to network`() {
    assertThat(Stubs.message.toNetwork())
      .isDataClassEqualTo(Stubs.messageResponse)
  }

  @Test fun `message create request to domain`() {
    assertThat(Stubs.messageTemplateCreateRequest.toDomain(Stubs.project.id))
      .isDataClassEqualTo(Stubs.messageTemplateCreator)
  }

  @Test fun `message update network to domain (empty)`() {
    assertThat(MessageTemplateUpdateRequest().toDomain(Stubs.messageTemplate.id))
      .isDataClassEqualTo(MessageTemplateUpdater(Stubs.messageTemplate.id))
  }

  @Test fun `message update network to domain (filled)`() {
    assertThat(Stubs.messageTemplateUpdateRequest.toDomain(Stubs.messageTemplate.id))
      .isDataClassEqualTo(Stubs.messageTemplateUpdater)
  }

  @Test fun `message inputs network to domain (empty)`() {
    assertThat(MessageInputsRequest().toDomain())
      .isDataClassEqualTo(Inputs())
  }

  @Test fun `message inputs network to domain (filled)`() {
    assertThat(Stubs.messageInputsRequest.toDomain())
      .isDataClassEqualTo(Stubs.messageInputs)
  }

}
