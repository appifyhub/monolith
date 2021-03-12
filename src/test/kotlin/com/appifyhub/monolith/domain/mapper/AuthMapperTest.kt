package com.appifyhub.monolith.domain.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class AuthMapperTest {

  @Test fun `owned token data to domain`() {
    assertThat(Stubs.ownedTokenDbm.toDomain()).isDataClassEqualTo(
      Stubs.ownedToken.copy(
        owner = Stubs.user.copy(
          ownedTokens = emptyList(), // no info about this on data layer
        ),
      )
    )
  }

  @Test fun `owned token domain to data`() {
    assertThat(Stubs.ownedToken.toData(Stubs.project)).isEqualTo(Stubs.ownedTokenDbm)
  }

  @Test fun `token data to domain`() {
    assertThat(Stubs.tokenDbm.toDomain()).isDataClassEqualTo(Stubs.token)
  }

}
