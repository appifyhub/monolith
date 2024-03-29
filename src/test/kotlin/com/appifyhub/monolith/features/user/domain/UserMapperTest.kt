package com.appifyhub.monolith.features.user.domain

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class UserMapperTest {

  @Test fun `user data to domain`() {
    val result = Stubs.userDbm.toDomain()

    assertThat(result).isDataClassEqualTo(Stubs.user)
  }

  @Test fun `user domain to data`() {
    assertThat(Stubs.user.toData(Stubs.project)).isEqualTo(Stubs.userDbm)
  }

  @Test fun `user ID data to domain`() {
    assertThat(Stubs.userIdDbm.toDomain()).isDataClassEqualTo(Stubs.userId)
  }

  @Test fun `user ID domain to data`() {
    assertThat(Stubs.userId.toData()).isEqualTo(Stubs.userIdDbm)
  }

  @Test fun `organization data to domain`() {
    assertThat(Stubs.companyDbm.toDomain()).isDataClassEqualTo(Stubs.company)
  }

  @Test fun `organization domain to data`() {
    assertThat(Stubs.company.toData()).isEqualTo(Stubs.companyDbm)
  }

}
