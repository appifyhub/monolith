package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.service.access.AccessManager.Feature
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class CreatorProjectMapperTest {

  @Test fun `feature - domain to network`() {
    assertThat(Feature.BASIC.toNetwork())
      .isDataClassEqualTo(Stubs.projectFeatureResponse)
  }

  @Test fun `project state - domain to network`() {
    assertThat(Stubs.projectState.toNetwork())
      .isDataClassEqualTo(Stubs.projectStateResponse)
  }

  @Test fun `project - domain to network`() {
    assertThat(Stubs.project.toNetwork(projectState = Stubs.projectState))
      .isDataClassEqualTo(Stubs.projectResponse)
  }

}
