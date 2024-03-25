package com.appifyhub.monolith.features.user.domain

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class PushDeviceMapperTest {

  @Test fun `push device data to domain`() {
    assertThat(Stubs.pushDeviceDbm.toDomain()).isDataClassEqualTo(Stubs.pushDevice)
  }

  @Test fun `push device domain to data`() {
    assertThat(Stubs.pushDevice.toData(Stubs.project)).isEqualTo(Stubs.pushDeviceDbm)
  }

}
