package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class CreatorMapperTest {

  @Test fun `property filter query params - network to domain`() {
    assertThat(Stubs.propertyFilterQueryParams.toDomain())
      .isDataClassEqualTo(Stubs.propertyFilter)
  }

  @Test fun `property configuration - domain to network`() {
    assertThat(Stubs.propString.config.toNetwork())
      .isDataClassEqualTo(Stubs.propertyConfigurationResponse)
  }

  @Test fun `property - domain to network`() {
    assertThat(Stubs.propString.toNetwork())
      .isDataClassEqualTo(Stubs.propertyResponse)
  }

}
