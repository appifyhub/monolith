package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class UserMapperTest {

  @Test fun `user domain to network`() {
    val user = Stubs.user.copy(
      birthday = DateTimeMapper.parseAsDateTime("1970-05-14 00:00"), // from the stub
      createdAt = DateTimeMapper.parseAsDateTime("1970-05-14 03:04"), // from the stub
      updatedAt = DateTimeMapper.parseAsDateTime("1970-05-15 05:06"), // from the stub
    ).toNetwork()

    assertThat(user)
      .isDataClassEqualTo(Stubs.userResponse)
  }

  @Test fun `organization network to domain`() {
    assertThat(Stubs.companyDto.toDomain())
      .isDataClassEqualTo(Stubs.company)
  }

  @Test fun `organization domain to network`() {
    assertThat(Stubs.company.toNetwork())
      .isDataClassEqualTo(Stubs.companyDto)
  }

}
