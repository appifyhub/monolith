package com.appifyhub.monolith.validation.impl

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.appifyhub.monolith.domain.integrations.MailgunConfig
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import org.junit.jupiter.api.Test

class ValidatorsTest {

  // region Generic validators

  @Test fun `not blank fails with null`() {
    assertThat(Validators.NotBlank.isValid(null))
      .isFalse()
  }

  @Test fun `not blank fails with blank`() {
    assertThat(Validators.NotBlank.isValid(" \t\n "))
      .isFalse()
  }

  @Test fun `not blank succeeds with content`() {
    assertThat(Validators.NotBlank.isValid("b"))
      .isTrue()
  }

  @Test fun `nullable not blank succeeds with null`() {
    assertThat(Validators.NotBlankNullable.isValid(null))
      .isTrue()
  }

  @Test fun `nullable not blank fails with blank`() {
    assertThat(Validators.NotBlankNullable.isValid(" \t\n "))
      .isFalse()
  }

  @Test fun `nullable not blank succeeds with content`() {
    assertThat(Validators.NotBlankNullable.isValid("b"))
      .isTrue()
  }

  @Test fun `no spaces fails with null`() {
    assertThat(Validators.NoSpaces.isValid(null))
      .isFalse()
  }

  @Test fun `no spaces fails with empty`() {
    assertThat(Validators.NoSpaces.isValid(""))
      .isFalse()
  }

  @Test fun `no spaces fails with spaces`() {
    assertThat(Validators.NoSpaces.isValid(" "))
      .isFalse()
  }

  @Test fun `no spaces fails with whitespace`() {
    assertThat(Validators.NoSpaces.isValid("\n\t"))
      .isFalse()
  }

  @Test fun `no spaces succeeds with dense content`() {
    assertThat(Validators.NoSpaces.isValid("b"))
      .isTrue()
  }

  @Test fun `nullable no spaces fails with null`() {
    assertThat(Validators.NoSpacesNullable.isValid(null))
      .isTrue()
  }

  @Test fun `nullable no spaces fails with empty`() {
    assertThat(Validators.NoSpacesNullable.isValid(""))
      .isFalse()
  }

  @Test fun `nullable no spaces fails with spaces`() {
    assertThat(Validators.NoSpacesNullable.isValid(" "))
      .isFalse()
  }

  @Test fun `nullable no spaces fails with whitespace`() {
    assertThat(Validators.NoSpacesNullable.isValid("\n\t"))
      .isFalse()
  }

  @Test fun `nullable no spaces succeeds with dense content`() {
    assertThat(Validators.NoSpacesNullable.isValid("b"))
      .isTrue()
  }

  @Test fun `positive long fails with null`() {
    assertThat(Validators.PositiveLong.isValid(null))
      .isFalse()
  }

  @Test fun `positive long fails with zero`() {
    assertThat(Validators.PositiveLong.isValid(0L))
      .isFalse()
  }

  @Test fun `positive long succeeds with positive`() {
    assertThat(Validators.PositiveLong.isValid(1L))
      .isTrue()
  }

  @Test fun `flag fails with null`() {
    assertThat(Validators.Flag.isValid(null))
      .isFalse()
  }

  @Test fun `flag succeeds with false`() {
    assertThat(Validators.Flag.isValid(false))
      .isTrue()
  }

  @Test fun `flag succeeds with true`() {
    assertThat(Validators.Flag.isValid(true))
      .isTrue()
  }

  // endregion

  // region ID validators

  @Test fun `custom user ID is no spaces`() {
    assertThat(Validators.CustomUserId)
      .isEqualTo(Validators.NoSpaces)
  }

  @Test fun `username is no spaces`() {
    assertThat(Validators.Username)
      .isEqualTo(Validators.NoSpaces)
  }

  @Test fun `raw signature is non blank`() {
    assertThat(Validators.RawSignature)
      .isEqualTo(Validators.NotBlank)
  }

  @Test fun `nullable raw signature is non blank nullable`() {
    assertThat(Validators.RawSignatureNullable)
      .isEqualTo(Validators.NotBlankNullable)
  }

  @Test fun `user ID fails with spaces identifier`() {
    assertThat(Validators.UserId.isValid(UserId(" ", 1)))
      .isFalse()
  }

  @Test fun `user ID fails with negative project ID`() {
    assertThat(Validators.UserId.isValid(UserId("b", -1)))
      .isFalse()
  }

  @Test fun `user ID succeeds with content identifier and positive project ID`() {
    assertThat(Validators.UserId.isValid(UserId("b", 1)))
      .isTrue()
  }

  // endregion

  // region Contact validators

  @Test fun `name is non blank nullable`() {
    assertThat(Validators.Name)
      .isEqualTo(Validators.NotBlankNullable)
  }

  @Test fun `custom contact is non blank nullable`() {
    assertThat(Validators.CustomContact)
      .isEqualTo(Validators.NotBlankNullable)
  }

  @Test fun `email fails with null`() {
    assertThat(Validators.Email.isValid(null))
      .isFalse()
  }

  @Test fun `email fails with spaces`() {
    assertThat(Validators.Email.isValid(" "))
      .isFalse()
  }

  @Test fun `email fails with invalid`() {
    assertThat(Validators.Email.isValid("invalid"))
      .isFalse()
  }

  @Test fun `email fails with username only`() {
    assertThat(Validators.Email.isValid("username@"))
      .isFalse()
  }

  @Test fun `email fails with domain only`() {
    assertThat(Validators.Email.isValid("@domain.com"))
      .isFalse()
  }

  @Test fun `email fails with local email`() {
    assertThat(Validators.Email.isValid("username@domain"))
      .isFalse()
  }

  @Test fun `email fails with space char`() {
    assertThat(Validators.Email.isValid("user name@domain.com"))
      .isFalse()
  }

  @Test fun `email succeeds with real address`() {
    assertThat(Validators.Email.isValid("username@domain.com"))
      .isTrue()
  }

  @Test fun `phone fails with null`() {
    assertThat(Validators.Phone.isValid(null))
      .isFalse()
  }

  @Test fun `phone fails with spaces`() {
    assertThat(Validators.Phone.isValid(" "))
      .isFalse()
  }

  @Test fun `phone fails with whitespaces`() {
    assertThat(Validators.Phone.isValid("\t\n"))
      .isFalse()
  }

  @Test fun `phone fails with plus only`() {
    assertThat(Validators.Phone.isValid("+"))
      .isFalse()
  }

  @Test fun `phone fails with too short number`() {
    assertThat(Validators.Phone.isValid("+123"))
      .isFalse()
  }

  @Test fun `phone fails with local format`() {
    assertThat(Validators.Phone.isValid("01760000000"))
      .isFalse()
  }

  @Test fun `phone fails with international 00-format`() {
    assertThat(Validators.Phone.isValid("00491760000000"))
      .isFalse()
  }

  @Test fun `phone fails with international format`() {
    assertThat(Validators.Phone.isValid("+491760000000"))
      .isTrue()
  }

  // endregion

  // region Organization validators

  @Test fun `orga name is non blank nullable`() {
    assertThat(Validators.OrganizationName)
      .isEqualTo(Validators.NotBlankNullable)
  }

  @Test fun `orga street is non blank nullable`() {
    assertThat(Validators.OrganizationStreet)
      .isEqualTo(Validators.NotBlankNullable)
  }

  @Test fun `orga postcode is non blank nullable`() {
    assertThat(Validators.OrganizationPostcode)
      .isEqualTo(Validators.NotBlankNullable)
  }

  @Test fun `orga city is non blank nullable`() {
    assertThat(Validators.OrganizationCity)
      .isEqualTo(Validators.NotBlankNullable)
  }

  @Test fun `orga country code succeeds with null`() {
    assertThat(Validators.OrganizationCountryCode.isValid(null))
      .isTrue()
  }

  @Test fun `orga country code fails with spaces`() {
    assertThat(Validators.OrganizationCountryCode.isValid(" "))
      .isFalse()
  }

  @Test fun `orga country code fails with whitespace`() {
    assertThat(Validators.OrganizationCountryCode.isValid("\t\n"))
      .isFalse()
  }

  @Test fun `orga country code fails with invalid chars`() {
    assertThat(Validators.OrganizationCountryCode.isValid("33"))
      .isFalse()
  }

  @Test fun `orga country code fails when too long`() {
    assertThat(Validators.OrganizationCountryCode.isValid("DEX"))
      .isFalse()
  }

  @Test fun `orga country code fails when too short`() {
    assertThat(Validators.OrganizationCountryCode.isValid("D"))
      .isFalse()
  }

  @Test fun `orga country code fails with lowercase`() {
    assertThat(Validators.OrganizationCountryCode.isValid("de"))
      .isFalse()
  }

  @Test fun `orga country code succeeds with valid country code`() {
    assertThat(Validators.OrganizationCountryCode.isValid("DE"))
      .isTrue()
  }

  @Test fun `orga succeeds with null`() {
    assertThat(Validators.Organization.isValid(null))
      .isTrue()
  }

  @Test fun `orga succeeds with all null properties`() {
    assertThat(Validators.Organization.isValid(Organization.EMPTY))
      .isTrue()
  }

  @Test fun `orga fails with at least one invalid property`() {
    listOf(
      Organization.EMPTY.copy(name = " "),
      Organization.EMPTY.copy(street = " "),
      Organization.EMPTY.copy(postcode = " "),
      Organization.EMPTY.copy(city = " "),
      Organization.EMPTY.copy(countryCode = " "),
    ).forEach {
      assertThat(Validators.Organization.isValid(it))
        .isFalse()
    }
  }

  @Test fun `orga succeeds with valid content`() {
    assertThat(Validators.Organization.isValid(Stubs.company))
      .isTrue()
  }

  // endregion

  // region Project validators

  @Test fun `project ID is positive long`() {
    assertThat(Validators.ProjectId)
      .isEqualTo(Validators.PositiveLong)
  }

  @Test fun `project name is non blank`() {
    assertThat(Validators.ProjectName)
      .isEqualTo(Validators.NotBlank)
  }

  @Test fun `url fails with null`() {
    assertThat(Validators.Url.isValid(null))
      .isFalse()
  }

  @Test fun `url fails with blank`() {
    assertThat(Validators.Url.isValid(" \t \n "))
      .isFalse()
  }

  @Test fun `url fails with invalid format`() {
    assertThat(Validators.Url.isValid("invalid"))
      .isFalse()
  }

  @Test fun `url succeeds with valid content`() {
    assertThat(Validators.Url.isValid("https://appifyhub.com"))
      .isTrue()
  }

  @Test fun `nullable url fails with blank`() {
    assertThat(Validators.UrlNullable.isValid(" \t \n "))
      .isFalse()
  }

  @Test fun `nullable url fails with invalid format`() {
    assertThat(Validators.UrlNullable.isValid("invalid"))
      .isFalse()
  }

  @Test fun `nullable url succeeds with null`() {
    assertThat(Validators.UrlNullable.isValid(null))
      .isTrue()
  }

  @Test fun `nullable url succeeds with valid content`() {
    assertThat(Validators.UrlNullable.isValid("https://appifyhub.com"))
      .isTrue()
  }

  // endregion

  // region Other validators

  @Test fun `origin is non blank nullable`() {
    assertThat(Validators.Origin)
      .isEqualTo(Validators.NotBlankNullable)
  }

  @Test fun `IP address fails with blank`() {
    assertThat(Validators.IpAddress.isValid("\t\n"))
      .isFalse()
  }

  @Test fun `IP address fails with invalid address`() {
    assertThat(Validators.IpAddress.isValid("12.142414"))
      .isFalse()
  }

  @Test fun `IP address succeeds with null`() {
    assertThat(Validators.IpAddress.isValid(null))
      .isTrue()
  }

  @Test fun `IP address succeeds with valid IPv4 address`() {
    assertThat(Validators.IpAddress.isValid("1.2.3.4"))
      .isTrue()
  }

  @Test fun `IP address succeeds with valid IPv6 address`() {
    assertThat(Validators.IpAddress.isValid("2001:db8:3333:4444:5555:6666:7777:8888"))
      .isTrue()
  }

  @Test fun `birthday succeeds with null`() {
    assertThat(Validators.BDay.isValid(null))
      .isTrue()
  }

  @Test fun `birthday fails if after today`() {
    val timeProvider = TimeProviderFake { 1000 }
    val birthday: BDay = Date(10000) to timeProvider

    assertThat(Validators.BDay.isValid(birthday))
      .isFalse()
  }

  @Test fun `birthday fails if less than 10 years old`() {
    val tenYearsMillis = ChronoUnit.YEARS.duration.multipliedBy(5).toMillis()
    val timeProvider = TimeProviderFake { tenYearsMillis }
    val birthday: BDay = Date(0) to timeProvider

    assertThat(Validators.BDay.isValid(birthday))
      .isFalse()
  }

  @Test fun `birthday fails if more than 100 years old`() {
    val tenYearsMillis = ChronoUnit.YEARS.duration.multipliedBy(110).toMillis()
    val timeProvider = TimeProviderFake { tenYearsMillis }
    val birthday: BDay = Date(0) to timeProvider

    assertThat(Validators.BDay.isValid(birthday))
      .isFalse()
  }

  @Test fun `birthday succeeds with age between 10 and 100`() {
    val fiftyYearsMillis = ChronoUnit.YEARS.duration.multipliedBy(50).toMillis()
    val timeProvider = TimeProviderFake { fiftyYearsMillis }
    val birthday: BDay = Date(0) to timeProvider

    assertThat(Validators.BDay.isValid(birthday))
      .isTrue()
  }

  @Test fun `language tag fails if blank`() {
    assertThat(Validators.LanguageTag.isValid(" \n\t "))
      .isFalse()
  }

  @Test fun `language tag fails if invalid code`() {
    assertThat(Validators.LanguageTag.isValid("asdasdasdasd"))
      .isFalse()
  }

  @Test fun `language tag succeeds if null`() {
    assertThat(Validators.LanguageTag.isValid(null))
      .isTrue()
  }

  @Test fun `language tag succeeds if valid code`() {
    assertThat(Validators.LanguageTag.isValid(Locale.US.toLanguageTag()))
      .isTrue()
  }

  @Test fun `message template ID is positive long`() {
    assertThat(Validators.MessageTemplateId)
      .isEqualTo(Validators.PositiveLong)
  }

  @Test fun `message template name fails if null`() {
    assertThat(Validators.MessageTemplateName.isValid(null))
      .isFalse()
  }

  @Test fun `message template name fails if blank`() {
    assertThat(Validators.MessageTemplateName.isValid("\n\t"))
      .isFalse()
  }

  @Test fun `message template name fails with invalid characters`() {
    assertThat(Validators.MessageTemplateName.isValid("Almost_All-Are!VALID"))
      .isFalse()
  }

  @Test fun `message template name succeeds with valid characters`() {
    assertThat(Validators.MessageTemplateName.isValid("All_Chars-Are-1000-VALID"))
      .isTrue()
  }

  @Test fun `message template is non blank`() {
    assertThat(Validators.MessageTemplate)
      .isEqualTo(Validators.NotBlank)
  }

  // endregion

  // region Integrations validators

  @Test fun `mailgun config fails with invalid API key`() {
    val config = MailgunConfig(
      apiKey = "api-key 123456 abc",
      domain = "domain.com",
      senderName = "na me",
      senderEmail = "email@domain.com",
    )
    assertThat(Validators.MailgunConfigData.isValid(config))
      .isFalse()
  }

  @Test fun `mailgun config fails with invalid domain`() {
    val config = MailgunConfig(
      apiKey = "api-key:123456abc",
      domain = "domain com",
      senderName = "na me",
      senderEmail = "email@domain.com",
    )
    assertThat(Validators.MailgunConfigData.isValid(config))
      .isFalse()
  }

  @Test fun `mailgun config fails with invalid sender name`() {
    val config = MailgunConfig(
      apiKey = "api-key:123456abc",
      domain = "domain.com",
      senderName = "",
      senderEmail = "email@domain.com",
    )
    assertThat(Validators.MailgunConfigData.isValid(config))
      .isFalse()
  }

  @Test fun `mailgun config fails with invalid sender email`() {
    val config = MailgunConfig(
      apiKey = "api-key:123456abc",
      domain = "domain.com",
      senderName = "na me",
      senderEmail = "not_an_email",
    )
    assertThat(Validators.MailgunConfigData.isValid(config))
      .isFalse()
  }

  @Test fun `mailgun config succeeds with null`() {
    assertThat(Validators.MailgunConfigData.isValid(null))
      .isTrue()
  }

  @Test fun `mailgun config succeeds with valid data`() {
    val config = MailgunConfig(
      apiKey = "api-key:123456abc",
      domain = "domain.com",
      senderName = "na me",
      senderEmail = "email@domain.com",
    )
    assertThat(Validators.MailgunConfigData.isValid(config))
      .isTrue()
  }

  // endregion

}
