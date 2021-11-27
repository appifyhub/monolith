package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.service.access.AccessManager.Feature
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

  @Test fun `feature - domain to network`() {
    assertThat(Feature.BASIC.toNetwork())
      .isDataClassEqualTo(Stubs.projectFeatureDto)
  }

  @Test fun `project status - domain to network`() {
    assertThat(Stubs.projectStatus.toNetwork())
      .isDataClassEqualTo(Stubs.projectStatusDto)
  }

  @Test fun `project - domain to network`() {
    assertThat(Stubs.project.toNetwork(projectStatus = Stubs.projectStatus))
      .isDataClassEqualTo(Stubs.projectResponse)
  }

}
