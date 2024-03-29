package com.appifyhub.monolith.features.user.domain

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import com.appifyhub.monolith.features.common.domain.model.Settable
import com.appifyhub.monolith.features.user.domain.model.Organization
import com.appifyhub.monolith.features.user.domain.model.OrganizationUpdater
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.util.PasswordEncoderFake
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.Locale

class UserOpsMapperTest {

  @Test fun `user updater to user (no changes)`() {
    val userUpdater = UserUpdater(
      id = Stubs.userId,
      rawSignature = null,
      type = null,
      authority = null,
      contactType = null,
      allowsSpam = null,
      name = null,
      contact = null,
      verificationToken = null,
      birthday = null,
      company = null,
      languageTag = null,
    )

    val result = userUpdater.applyTo(
      user = Stubs.user,
      passwordEncoder = PasswordEncoderFake(),
      timeProvider = TimeProviderFake(incrementalTime = 10L)
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.user.copy(
        updatedAt = Date(10L),
      )
    )
  }

  @Test fun `user updater to user (with changes to non-null)`() {
    val userUpdater = UserUpdater(
      id = Stubs.userId,
      rawSignature = Settable("password 2"),
      type = Settable(User.Type.PERSONAL),
      authority = Settable(User.Authority.DEFAULT),
      contactType = Settable(User.ContactType.PHONE),
      allowsSpam = Settable(false),
      name = Settable("User's Name 2"),
      contact = Settable("user2@example.com"),
      verificationToken = Settable("abcd1234 2"),
      birthday = Settable(Date(0xB10000)),
      company = Settable(
        OrganizationUpdater(
          name = Settable("Company 2"),
          street = Settable("Street Name 2"),
          postcode = Settable("12345 2"),
          city = Settable("City 2"),
          countryCode = Settable("NL"),
        )
      ),
      languageTag = Settable(Locale.UK.toLanguageTag()),
    )

    val result = userUpdater.applyTo(
      user = Stubs.user,
      passwordEncoder = PasswordEncoderFake(),
      timeProvider = TimeProviderFake(incrementalTime = 10L),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.user.copy(
        signature = "2 drowssap",
        type = User.Type.PERSONAL,
        authority = User.Authority.DEFAULT,
        contactType = User.ContactType.PHONE,
        allowsSpam = false,
        name = "User's Name 2",
        contact = "user2@example.com",
        verificationToken = "abcd1234 2",
        birthday = Date(0xB10000),
        company = Organization(
          name = "Company 2",
          street = "Street Name 2",
          postcode = "12345 2",
          city = "City 2",
          countryCode = "NL",
        ),
        languageTag = Locale.UK.toLanguageTag(),
        updatedAt = Date(10L),
      )
    )
  }

  @Test fun `user updater to user (with changes to null)`() {
    val userUpdater = UserUpdater(
      id = Stubs.userId,
      name = Settable(null),
      contact = Settable(null),
      verificationToken = Settable(null),
      birthday = Settable(null),
      company = Settable(null),
      languageTag = Settable(null),
    )

    val result = userUpdater.applyTo(
      user = Stubs.user,
      passwordEncoder = PasswordEncoderFake(),
      timeProvider = TimeProviderFake(incrementalTime = 10L),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.user.copy(
        name = null,
        contact = null,
        verificationToken = null,
        birthday = null,
        company = null,
        languageTag = null,
        updatedAt = Date(10L),
      )
    )
  }

  @Test fun `user updater to user (with company properties removed)`() {
    val userUpdater = UserUpdater(
      id = Stubs.userId,
      company = Settable(
        OrganizationUpdater(
          name = Settable(null),
          street = Settable(null),
          postcode = Settable(null),
          city = Settable(null),
          countryCode = Settable(null),
        )
      ),
    )

    val result = userUpdater.applyTo(
      user = Stubs.user,
      passwordEncoder = PasswordEncoderFake(),
      timeProvider = TimeProviderFake(incrementalTime = 10L),
    )

    assertThat(result).isDataClassEqualTo(
      Stubs.user.copy(
        company = Organization(
          name = null,
          street = null,
          postcode = null,
          city = null,
          countryCode = null,
        ),
        updatedAt = Date(10L),
      )
    )
  }

  @Test fun `organization updater to organization (no changes)`() {
    val organizationUpdater = OrganizationUpdater(
      name = null,
      street = null,
      postcode = null,
      city = null,
      countryCode = null,
    )

    val result = organizationUpdater.applyTo(Stubs.company)!!

    assertThat(result).isDataClassEqualTo(Stubs.company)
  }

  @Test fun `organization updater to organization (with changes)`() {
    val organizationUpdater = OrganizationUpdater(
      name = Settable("Company 2"),
      street = Settable("Street Name 2"),
      postcode = Settable("12345 2"),
      city = Settable("City 2"),
      countryCode = Settable("NL"),
    )

    val result = organizationUpdater.applyTo(Stubs.company)!!

    assertThat(result).isDataClassEqualTo(
      Organization(
        name = "Company 2",
        street = "Street Name 2",
        postcode = "12345 2",
        city = "City 2",
        countryCode = "NL",
      )
    )
  }

  @Test fun `user creator to user (requesting new user ID)`() {
    val userCreator = UserCreator(
      userId = null,
      projectId = Stubs.project.id,
      rawSignature = "password",
      name = "User's Name",
      type = User.Type.ORGANIZATION,
      authority = User.Authority.ADMIN,
      allowsSpam = true,
      contact = "user@example.com",
      contactType = User.ContactType.EMAIL,
      birthday = Date(0xB00000),
      company = Stubs.company,
      languageTag = Locale.US.toLanguageTag(),
      signupCode = "FAKE-CODE-1234",
    )

    assertThat(
      userCreator.toUser(
        userId = "username",
        passwordEncoder = PasswordEncoderFake(),
        timeProvider = TimeProviderFake(staticTime = { 10L }),
      )
    ).isDataClassEqualTo(
      Stubs.user.copy(
        verificationToken = null,
        createdAt = Date(10L),
        updatedAt = Date(10L),
      )
    )
  }

  @Test fun `user creator to user (existing user ID)`() {
    val userCreator = UserCreator(
      userId = "username",
      projectId = Stubs.project.id,
      rawSignature = "password",
      name = "User's Name",
      type = User.Type.ORGANIZATION,
      authority = User.Authority.ADMIN,
      allowsSpam = true,
      contact = "user@example.com",
      contactType = User.ContactType.EMAIL,
      birthday = Date(0xB00000),
      company = Stubs.company,
      languageTag = Locale.US.toLanguageTag(),
      signupCode = "FAKE-CODE-1234",
    )

    assertThat(
      userCreator.toUser(
        userId = userCreator.userId!!,
        passwordEncoder = PasswordEncoderFake(),
        timeProvider = TimeProviderFake(staticTime = { 10L }),
      )
    ).isDataClassEqualTo(
      Stubs.user.copy(
        verificationToken = null,
        createdAt = Date(10L),
        updatedAt = Date(10L),
      )
    )
  }

}
