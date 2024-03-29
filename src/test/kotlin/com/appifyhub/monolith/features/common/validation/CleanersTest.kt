package com.appifyhub.monolith.features.common.validation

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.isZero
import com.appifyhub.monolith.features.creator.domain.model.messaging.FirebaseConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.MailgunConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.TwilioConfig
import com.appifyhub.monolith.features.user.domain.model.Organization
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.Date
import java.util.Locale

class CleanersTest {

  // region Generic cleaners

  @Test fun `trim with null is empty`() {
    assertThat(Cleaners.Trim.clean(null))
      .isEmpty()
  }

  @Test fun `trim with non-null works`() {
    assertThat(Cleaners.Trim.clean(" \na\t "))
      .isEqualTo("a")
  }

  @Test fun `nullable trim with null is null`() {
    assertThat(Cleaners.TrimNullable.clean(null))
      .isNull()
  }

  @Test fun `nullable trim with non-null works`() {
    assertThat(Cleaners.TrimNullable.clean(" \na\t "))
      .isEqualTo("a")
  }

  @Test fun `nullified trim with null is null`() {
    assertThat(Cleaners.TrimNullified.clean(null))
      .isNull()
  }

  @Test fun `nullified trim with blank is null`() {
    assertThat(Cleaners.TrimNullified.clean(" \n\t "))
      .isNull()
  }

  @Test fun `nullified trim with non-null works`() {
    assertThat(Cleaners.TrimNullified.clean(" \na\t "))
      .isEqualTo("a")
  }

  @Test fun `removing spaces with null is empty`() {
    assertThat(Cleaners.RemoveSpaces.clean(null))
      .isEmpty()
  }

  @Test fun `removing spaces with non-null `() {
    assertThat(Cleaners.RemoveSpaces.clean(" \na\t "))
      .isEqualTo("a")
  }

  @Test fun `nullable removing spaces with null is empty`() {
    assertThat(Cleaners.RemoveSpacesNullable.clean(null))
      .isNull()
  }

  @Test fun `nullable removing spaces with non-null `() {
    assertThat(Cleaners.RemoveSpacesNullable.clean(" \na b\t "))
      .isEqualTo("ab")
  }

  @Test fun `long to cardinal with null is zero`() {
    assertThat(Cleaners.LongToCardinal.clean(null))
      .isZero()
  }

  @Test fun `long to cardinal with negative is zero`() {
    assertThat(Cleaners.LongToCardinal.clean(-5))
      .isZero()
  }

  @Test fun `long to cardinal with positive works`() {
    assertThat(Cleaners.LongToCardinal.clean(5))
      .isEqualTo(5)
  }

  @Test fun `flag defaulting to false with null is false`() {
    assertThat(Cleaners.FlagDefFalse.clean(null))
      .isFalse()
  }

  @Test fun `flag defaulting to false with false is false`() {
    assertThat(Cleaners.FlagDefFalse.clean(false))
      .isFalse()
  }

  @Test fun `flag defaulting to false with true is true`() {
    assertThat(Cleaners.FlagDefFalse.clean(true))
      .isTrue()
  }

  @Test fun `flag defaulting to true with null is true`() {
    assertThat(Cleaners.FlagDefTrue.clean(null))
      .isTrue()
  }

  @Test fun `flag defaulting to true with false is false`() {
    assertThat(Cleaners.FlagDefTrue.clean(false))
      .isFalse()
  }

  @Test fun `flag defaulting to true with true is true`() {
    assertThat(Cleaners.FlagDefTrue.clean(true))
      .isTrue()
  }

  // endregion

  // region ID domain cleaners

  @Test fun `custom user ID is removing spaces`() {
    assertThat(Cleaners.CustomUserId)
      .isEqualTo(Cleaners.RemoveSpaces)
  }

  @Test fun `username is removing spaces`() {
    assertThat(Cleaners.Username)
      .isEqualTo(Cleaners.RemoveSpaces)
  }

  @Test fun `raw signature is trimming`() {
    assertThat(Cleaners.RawSignature)
      .isEqualTo(Cleaners.Trim)
  }

  @Test fun `nullable raw signature is trimming nullable`() {
    assertThat(Cleaners.RawSignatureNullified)
      .isEqualTo(Cleaners.TrimNullified)
  }

  @Test fun `user ID with space and negative is empty and zero`() {
    assertThat(Cleaners.UserId.clean(UserId("\t\n ", -1)))
      .isEqualTo(UserId("", 0))
  }

  @Test fun `user ID is removing spaces from identifier and making project ID cardinal`() {
    assertThat(Cleaners.UserId.clean(UserId(" \na b\t ", 5)))
      .isEqualTo(UserId("ab", 5))
  }

  // endregion

  // region Contact cleaners

  @Test fun `name is trimming nullable`() {
    assertThat(Cleaners.Name)
      .isEqualTo(Cleaners.TrimNullable)
  }

  @Test fun `custom contact is trimming nullable`() {
    assertThat(Cleaners.CustomContact)
      .isEqualTo(Cleaners.TrimNullable)
  }

  @Test fun `email is removing spaces`() {
    assertThat(Cleaners.Email)
      .isEqualTo(Cleaners.RemoveSpaces)
  }

  @Test fun `phone with null is empty`() {
    assertThat(Cleaners.Phone.clean(null))
      .isEmpty()
  }

  @Test fun `phone with no chars is empty`() {
    assertThat(Cleaners.Phone.clean(""))
      .isEmpty()
  }

  @Test fun `phone with alpha chars only is empty`() {
    assertThat(Cleaners.Phone.clean("invalid"))
      .isEqualTo("")
  }

  @Test fun `phone with alpha chars keeps only digits`() {
    assertThat(Cleaners.Phone.clean("abc123"))
      .isEqualTo("+123")
  }

  @Test fun `phone with local format becomes international`() {
    assertThat(Cleaners.Phone.clean("0123"))
      .isEqualTo("+123")
  }

  @Test fun `phone with international 00-format becomes international`() {
    assertThat(Cleaners.Phone.clean("00123"))
      .isEqualTo("+123")
  }

  @Test fun `phone with international format works`() {
    assertThat(Cleaners.Phone.clean("+123"))
      .isEqualTo("+123")
  }

  // endregion

  // region Organization cleaners

  @Test fun `orga name is trimming nullable`() {
    assertThat(Cleaners.OrganizationName)
      .isEqualTo(Cleaners.TrimNullable)
  }

  @Test fun `orga street is trimming nullable`() {
    assertThat(Cleaners.OrganizationStreet)
      .isEqualTo(Cleaners.TrimNullable)
  }

  @Test fun `orga postcode is trimming nullable`() {
    assertThat(Cleaners.OrganizationPostcode)
      .isEqualTo(Cleaners.TrimNullable)
  }

  @Test fun `orga city is trimming nullable`() {
    assertThat(Cleaners.OrganizationCity)
      .isEqualTo(Cleaners.TrimNullable)
  }

  @Test fun `orga country code with null is null`() {
    assertThat(Cleaners.OrganizationCountryCode.clean(null))
      .isNull()
  }

  @Test fun `orga country code with blank is empty`() {
    assertThat(Cleaners.OrganizationCountryCode.clean(" \t\n "))
      .isNull()
  }

  @Test fun `orga country code with one char is one char`() {
    assertThat(Cleaners.OrganizationCountryCode.clean("D"))
      .isEqualTo("D")
  }

  @Test fun `orga country code with more chars keeps only 2`() {
    assertThat(Cleaners.OrganizationCountryCode.clean("DEX"))
      .isEqualTo("DE")
  }

  @Test fun `orga country code with lowercase will uppercase`() {
    assertThat(Cleaners.OrganizationCountryCode.clean("de"))
      .isEqualTo("DE")
  }

  @Test fun `orga country code with valid code works`() {
    assertThat(Cleaners.OrganizationCountryCode.clean("DE"))
      .isEqualTo("DE")
  }

  @Test fun `orga with nulls is null`() {
    assertThat(Cleaners.Organization.clean(Organization.EMPTY))
      .isNull()
  }

  @Test fun `orga with blanks is null`() {
    assertThat(
      Cleaners.Organization.clean(
        Organization(
          name = " \t\n ",
          street = " \t\n ",
          postcode = " \t\n ",
          city = " \t\n ",
          countryCode = " \t\n ",
        ),
      ),
    ).isNull()
  }

  @Test fun `orga with one non-blank property works`() {
    assertThat(
      Cleaners.Organization.clean(
        Organization(
          name = " \t\n ",
          street = " \tstreet\n ",
          postcode = " \t\n ",
          city = null,
          countryCode = null,
        ),
      ),
    ).isEqualTo(
      Organization(
        name = "",
        street = "street",
        postcode = "",
        city = null,
        countryCode = null,
      ),
    )
  }

  @Test fun `orga with all non-blank properties works`() {
    assertThat(Cleaners.Organization.clean(Stubs.company))
      .isEqualTo(Stubs.company)
  }

  // endregion

  // region Project cleaners

  @Test fun `project ID is long-to-cardinal`() {
    assertThat(Cleaners.ProjectId)
      .isEqualTo(Cleaners.LongToCardinal)
  }

  @Test fun `project name is trimming`() {
    assertThat(Cleaners.ProjectName)
      .isEqualTo(Cleaners.Trim)
  }

  @Test fun `url is trimming`() {
    assertThat(Cleaners.Url)
      .isEqualTo(Cleaners.Trim)
  }

  @Test fun `nullified url is trimming nullified`() {
    assertThat(Cleaners.UrlNullified)
      .isEqualTo(Cleaners.TrimNullified)
  }

  // endregion

  // region Other cleaners

  @Test fun `origin is trimming nullable`() {
    assertThat(Cleaners.Origin)
      .isEqualTo(Cleaners.TrimNullable)
  }

  @Test fun `IP address is removing spaces nullable`() {
    assertThat(Cleaners.IpAddress)
      .isEqualTo(Cleaners.RemoveSpacesNullable)
  }

  @Test fun `birthday is doing nothing`() {
    val birthday: BDay = Date() to mock()
    assertThat(Cleaners.BDay.clean(birthday))
      .isEqualTo(birthday)
  }

  @Test fun `language tag defaulting to null with null`() {
    assertThat(Cleaners.LanguageTagNullified.clean(null))
      .isNull()
  }

  @Test fun `language tag defaulting to null with empty`() {
    assertThat(Cleaners.LanguageTagNullified.clean(""))
      .isNull()
  }

  @Test fun `language tag defaulting to null with invalid code`() {
    assertThat(Cleaners.LanguageTagNullified.clean("asdasdasdasd"))
      .isNull()
  }

  @Test fun `language tag cleans trimmed valid code`() {
    val tag = Locale.US.toLanguageTag()
    assertThat(Cleaners.LanguageTagNullified.clean(" \n$tag\t "))
      .isEqualTo(tag)
  }

  @Test fun `message template ID is long-to-cardinal`() {
    assertThat(Cleaners.MessageTemplateId)
      .isEqualTo(Cleaners.LongToCardinal)
  }

  @Test fun `message template name defaulting to empty with null`() {
    assertThat(Cleaners.MessageTemplateName.clean(null))
      .isEmpty()
  }

  @Test fun `message template name removes invalid chars`() {
    assertThat(Cleaners.MessageTemplateName.clean("Some-Are_valid-1000!@#$%^&*()"))
      .isEqualTo("Some-Are_valid-1000")
  }

  @Test fun `message template is trimming`() {
    assertThat(Cleaners.MessageTemplate)
      .isEqualTo(Cleaners.Trim)
  }

  @Test fun `push device token is removing spaces`() {
    assertThat(Cleaners.PushDeviceToken)
      .isEqualTo(Cleaners.RemoveSpaces)
  }

  @Test fun `signup code is removing spaces`() {
    assertThat(Cleaners.SignupCode)
      .isEqualTo(Cleaners.RemoveSpaces)
  }

  // endregion

  // region Integrations cleaners

  @Test fun `mailgun config is cleaning with null`() {
    assertThat(Cleaners.MailgunConfigData.clean(null))
      .isNull()
  }

  @Test fun `mailgun config is cleaning`() {
    val dirty = MailgunConfig(
      apiKey = "a b c",
      domain = " domain. com ",
      senderName = " na me ",
      senderEmail = " email@domain.com ",
    )
    val expected = MailgunConfig(
      apiKey = "abc",
      domain = "domain.com",
      senderName = "na me",
      senderEmail = "email@domain.com",
    )
    assertThat(Cleaners.MailgunConfigData.clean(dirty))
      .isNotNull()
      .isDataClassEqualTo(expected)
  }

  @Test fun `twilio config is cleaning with null`() {
    assertThat(Cleaners.TwilioConfigData.clean(null))
      .isNull()
  }

  @Test fun `twilio config is cleaning`() {
    val dirty = TwilioConfig(
      accountSid = "a b c",
      authToken = "a u t h",
      messagingServiceId = "m i d",
      maxPricePerMessage = -10,
      maxRetryAttempts = -20,
      defaultSenderName = " \n \t ",
      defaultSenderNumber = "00123456789",
    )
    val expected = TwilioConfig(
      accountSid = "abc",
      authToken = "auth",
      messagingServiceId = "mid",
      maxPricePerMessage = 0,
      maxRetryAttempts = 0,
      defaultSenderName = "",
      defaultSenderNumber = "+123456789",
    )
    assertThat(Cleaners.TwilioConfigData.clean(dirty))
      .isNotNull()
      .isDataClassEqualTo(expected)
  }

  @Test fun `firebase config is cleaning with null`() {
    assertThat(Cleaners.FirebaseConfigData.clean(null))
      .isNull()
  }

  @Test fun `firebase config is cleaning`() {
    val dirty = FirebaseConfig(
      projectName = " Name with Spaces around ",
      serviceAccountKeyJsonBase64 = " 1234 test ",
    )
    val expected = FirebaseConfig(
      projectName = "Name with Spaces around",
      serviceAccountKeyJsonBase64 = "1234test",
    )
    assertThat(Cleaners.FirebaseConfigData.clean(dirty))
      .isNotNull()
      .isDataClassEqualTo(expected)
  }

  // endregion

}
