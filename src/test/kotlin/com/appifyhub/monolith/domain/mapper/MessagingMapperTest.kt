package com.appifyhub.monolith.domain.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class MessagingMapperTest {

  @Test fun `messaging template data to domain`() {
    assertThat(Stubs.messageTemplateDbm.toDomain())
      .isDataClassEqualTo(Stubs.messageTemplate)
  }

  @Test fun `messaging template domain to data`() {
    assertThat(Stubs.messageTemplate.toData(Stubs.project.id))
      .isEqualTo(Stubs.messageTemplateDbm)
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
