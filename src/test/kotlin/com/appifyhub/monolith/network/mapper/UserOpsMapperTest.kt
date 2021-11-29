package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.ops.UserCreatorRequest
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class UserOpsMapperTest {

  @Test fun `user updater network to domain`() {
    val updater = Stubs.userUpdaterRequest.toDomain(id = Stubs.userId)

    assertThat(updater).isDataClassEqualTo(
      Stubs.userUpdater.copy(
        verificationToken = null,
        birthday = Settable(DateTimeMapper.parseAsDateTime("1970-05-15 00:00")), // from the stub
      )
    )
  }

  @Test fun `organization updater network to domain`() {
    assertThat(Stubs.companyUpdaterDto.toDomain())
      .isDataClassEqualTo(Stubs.companyUpdater)
  }

  @Test fun `user creator request network to domain with defaults`() {
    val creator = UserCreatorRequest(
      userId = null,
      rawSignature = "s",
      name = null,
      type = null,
      authority = null,
      allowsSpam = null,
      contact = null,
      contactType = null,
      birthday = null,
      company = null,
    ).toDomain(projectId = Stubs.project.id)

    assertThat(creator).isDataClassEqualTo(
      UserCreator(
        userId = null,
        projectId = Stubs.project.id,
        rawSecret = "s",
        name = null,
        type = User.Type.PERSONAL,
        authority = User.Authority.DEFAULT,
        allowsSpam = false,
        contact = null,
        contactType = User.ContactType.CUSTOM,
        birthday = null,
        company = null,
      )
    )
  }

  @Test fun `user creator request network to domain with all data`() {
    val creator = Stubs.userCreatorRequest.toDomain(projectId = Stubs.project.id)

    assertThat(creator).isDataClassEqualTo(
      Stubs.userCreator.copy(
        birthday = DateTimeMapper.parseAsDateTime("1970-05-14 00:00"), // from the stub
      )
    )
  }

}
