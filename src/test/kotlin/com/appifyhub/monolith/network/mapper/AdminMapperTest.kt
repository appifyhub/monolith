package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class AdminMapperTest {

  @Test fun `property filter query params - network to domain`() {
    assertThat(Stubs.propertyFilterQueryParams.toDomain())
      .isDataClassEqualTo(Stubs.propertyFilter)
  }

}
