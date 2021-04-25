package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class AuthMapperTest {

  @Test fun `token domain to network`() {
    assertThat(Stubs.token.toNetwork()).isDataClassEqualTo(Stubs.tokenResponse)
  }

  @Test fun `owned token domain to network`() {
    val token = Stubs.ownedToken.copy(
      createdAt = DateTimeMapper.parseAsDateTime("1970-05-28 00:00"), // from the stub
      expiresAt = DateTimeMapper.parseAsDateTime("1970-06-19 00:00"), // from the stub
    )

    assertThat(token.toNetwork()).isDataClassEqualTo(Stubs.tokenDetailsResponse)
  }

}
