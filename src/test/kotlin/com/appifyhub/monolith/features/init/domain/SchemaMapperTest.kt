package com.appifyhub.monolith.features.init.domain

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class SchemaMapperTest {

  @Test fun `schema data to domain`() {
    assertThat(Stubs.schemaDbm.toDomain()).isDataClassEqualTo(Stubs.schema)
  }

  @Test fun `schema domain to data`() {
    assertThat(Stubs.schema.toData()).isEqualTo(Stubs.schemaDbm)
  }

}
