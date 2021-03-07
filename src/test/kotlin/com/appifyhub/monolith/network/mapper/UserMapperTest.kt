package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class UserMapperTest {

  @Test fun `user domain to network`() {
    val user = Stubs.user.copy(
      birthday = DateTimeMapper.parseAsDateTime("1970-05-15 00:00"), // from the stub
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

  /*
fun User.toNetwork(): UserResponse = UserResponse(
  userId = userId.id,
  projectId = userId.projectId,
  unifiedId = userId.toUnifiedFormat(),
  name = name,
  type = type.name.toLowerCase(),
  authority = authority.name.toLowerCase(),
  allowsSpam = allowsSpam,
  contact = contact,
  contactType = contactType.name.toLowerCase(),
  birthday = birthday?.let { DateTimeMapper.formatAsDate(it) },
  company = company?.toNetwork(),
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  updatedAt = DateTimeMapper.formatAsDateTime(updatedAt),
)

   */

}
