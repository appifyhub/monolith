package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class AuthMapperTest {

  @Test fun `token response of value`() {
    assertThat(tokenResponseOf(Stubs.tokenValue))
      .isDataClassEqualTo(Stubs.tokenResponse)
  }

  @Test fun `token details domain to network`() {
    // copying here might be unnecessary...
    val token = Stubs.tokenDetails.copy(
      createdAt = DateTimeMapper.parseAsDateTime("2021-05-01 19:35"), // from the stub
      expiresAt = DateTimeMapper.parseAsDateTime("2026-10-22 19:35"), // from the stub
    )

    assertThat(token.toNetwork())
      .isDataClassEqualTo(Stubs.tokenDetailsResponse)
  }

}
