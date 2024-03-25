package com.appifyhub.monolith.features.creator.api

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.api.DateTimeMapper
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class CreatorUserOpsMapperTest {

  @Test fun `creator signup request network to domain with defaults`() {
    val creator = Stubs.creatorSignupRequest.copy(
      birthday = null,
      company = null,
      signupCode = null,
    ).toDomain(projectId = Stubs.project.id)

    assertThat(creator).isDataClassEqualTo(
      Stubs.userCreator.copy(
        authority = User.Authority.DEFAULT,
        contact = Stubs.userId.userId,
        birthday = null,
        company = null,
        signupCode = null,
      )
    )
  }

  @Test fun `creator signup request network to domain`() {
    val request = Stubs.creatorSignupRequest.toDomain(Stubs.project.id)

    assertThat(request).isDataClassEqualTo(
      Stubs.userCreator.copy(
        authority = User.Authority.DEFAULT,
        contact = Stubs.userId.userId,
        birthday = DateTimeMapper.parseAsDateTime("1970-05-14 00:00"), // from the stub
      )
    )
  }

}
