package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class PushDeviceMapperTest {

  @Test fun `push device domain to network`() {
    assertThat(Stubs.pushDevice.toNetwork())
      .isDataClassEqualTo(Stubs.pushDeviceResponse)
  }

  @Test fun `push device collection domain to network`() {
    assertThat(listOf(Stubs.pushDevice).toNetwork())
      .isDataClassEqualTo(Stubs.pushDevicesResponse)
  }

  @Test fun `push device network to domain`() {
    assertThat(Stubs.pushDeviceRequest.toDomain(Stubs.user))
      .isDataClassEqualTo(Stubs.pushDevice)
  }

}
