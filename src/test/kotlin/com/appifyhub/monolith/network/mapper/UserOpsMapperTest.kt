package com.appifyhub.monolith.network.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.ops.UserSignupRequest
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test

class UserOpsMapperTest {

  @Test fun `user signup request network to domain with defaults`() {
    val request = UserSignupRequest(
      userId = null,
      rawSignature = "s",
      name = null,
      type = null,
      allowsSpam = null,
      contact = null,
      contactType = null,
      birthday = null,
      company = null,
      languageTag = null,
    ).toDomain(projectId = Stubs.project.id)

    assertThat(request).isDataClassEqualTo(
      UserCreator(
        userId = null,
        projectId = Stubs.project.id,
        rawSignature = "s",
        name = null,
        type = User.Type.PERSONAL,
        authority = User.Authority.DEFAULT,
        allowsSpam = false,
        contact = null,
        contactType = User.ContactType.CUSTOM,
        birthday = null,
        company = null,
        languageTag = null,
      )
    )
  }

  @Test fun `user signup request network to domain with all data`() {
    val request = Stubs.userSignupRequest.toDomain(projectId = Stubs.project.id)

    assertThat(request).isDataClassEqualTo(
      Stubs.userCreator.copy(
        authority = User.Authority.DEFAULT,
        birthday = DateTimeMapper.parseAsDateTime("1970-05-14 00:00"), // from the stub
      )
    )
  }

  @Test fun `organization updater network to domain`() {
    assertThat(Stubs.companyUpdaterDto.toDomain())
      .isDataClassEqualTo(Stubs.companyUpdater)
  }

  @Test fun `user update authority request network to domain`() {
    val updater = Stubs.userUpdateAuthorityRequest.toDomain(id = Stubs.userId)

    assertThat(updater).isDataClassEqualTo(
      UserUpdater(
        id = Stubs.userId,
        authority = Settable(Stubs.userUpdated.authority),
      )
    )
  }

  @Test fun `user update data request network to domain`() {
    val updater = Stubs.userUpdateDataRequest.toDomain(id = Stubs.userId)

    assertThat(updater).isDataClassEqualTo(
      Stubs.userUpdater.copy(
        rawSignature = null,
        authority = null,
        verificationToken = null,
        birthday = Settable(DateTimeMapper.parseAsDateTime("1978-11-15 00:00")), // from the stub
      )
    )
  }

  @Test fun `user update signature request network to domain`() {
    val updater = Stubs.userUpdateSignatureRequest.toDomain(id = Stubs.userId)

    assertThat(updater).isDataClassEqualTo(
      UserUpdater(
        id = Stubs.userId,
        rawSignature = Settable(Stubs.userUpdateSignatureRequest.rawSignatureNew),
      )
    )
  }

}
