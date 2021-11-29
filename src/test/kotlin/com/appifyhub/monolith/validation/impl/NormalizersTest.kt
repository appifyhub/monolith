package com.appifyhub.monolith.validation.impl

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.validation.Cleaner
import com.appifyhub.monolith.validation.Normalizer
import com.appifyhub.monolith.validation.Validator
import org.junit.jupiter.api.Test

class NormalizersTest {

  // region Generic normalizers

  @Test fun `trimmed = not blank + trim`() {
    assertThat(Normalizers.NotBlank)
      .consistsOf(Validators.NotBlank, Cleaners.Trim)
  }

  @Test fun `trimmed nullable = not blank nullable + trim nullable`() {
    assertThat(Normalizers.NotBlankNullable)
      .consistsOf(Validators.NotBlankNullable, Cleaners.TrimNullable)
  }

  @Test fun `dense = no spaces + remove spaces`() {
    assertThat(Normalizers.Dense)
      .consistsOf(Validators.NoSpaces, Cleaners.RemoveSpaces)
  }

  @Test fun `dense nullable = no spaces nullable + remove spaces nullable`() {
    assertThat(Normalizers.DenseNullable)
      .consistsOf(Validators.NoSpacesNullable, Cleaners.RemoveSpacesNullable)
  }

  @Test fun `cardinal = positive long + long to cardinal`() {
    assertThat(Normalizers.Cardinal)
      .consistsOf(Validators.PositiveLong, Cleaners.LongToCardinal)
  }

  @Test fun `flag def false = flag + flag def false`() {
    assertThat(Normalizers.FlagDefFalse)
      .consistsOf(Validators.Flag, Cleaners.FlagDefFalse)
  }

  @Test fun `flag def true = flag + flag def true`() {
    assertThat(Normalizers.FlagDefTrue)
      .consistsOf(Validators.Flag, Cleaners.FlagDefTrue)
  }

  // endregion

  // region Top level normalizers

  @Test fun `project ID = project ID + project ID`() {
    assertThat(Normalizers.ProjectId)
      .consistsOf(Validators.ProjectId, Cleaners.ProjectId)
  }

  @Test fun `account ID = account ID + account ID`() {
    assertThat(Normalizers.AccountId)
      .consistsOf(Validators.AccountId, Cleaners.AccountId)
  }

  // endregion

  // region ID normalizers

  @Test fun `custom user ID = custom user ID + custom user ID`() {
    assertThat(Normalizers.CustomUserId)
      .consistsOf(Validators.CustomUserId, Cleaners.CustomUserId)
  }

  @Test fun `username = username + username`() {
    assertThat(Normalizers.Username)
      .consistsOf(Validators.Username, Cleaners.Username)
  }

  @Test fun `raw signature = raw signature + raw signature`() {
    assertThat(Normalizers.RawSignature)
      .consistsOf(Validators.RawSignature, Cleaners.RawSignature)
  }

  @Test fun `raw signature nullable = raw signature nullable + raw signature nullable`() {
    assertThat(Normalizers.RawSignatureNullified)
      .consistsOf(Validators.RawSignatureNullable, Cleaners.RawSignatureNullified)
  }

  @Test fun `user ID = user ID + user ID`() {
    assertThat(Normalizers.UserId)
      .consistsOf(Validators.UserId, Cleaners.UserId)
  }

  // endregion

  // region Contact normalizers

  @Test fun `name = name + name`() {
    assertThat(Normalizers.Name)
      .consistsOf(Validators.Name, Cleaners.Name)
  }

  @Test fun `custom contact = custom contact + custom contact`() {
    assertThat(Normalizers.CustomContact)
      .consistsOf(Validators.CustomContact, Cleaners.CustomContact)
  }

  @Test fun `email = email + email`() {
    assertThat(Normalizers.Email)
      .consistsOf(Validators.Email, Cleaners.Email)
  }

  @Test fun `phone = phone + phone`() {
    assertThat(Normalizers.Phone)
      .consistsOf(Validators.Phone, Cleaners.Phone)
  }

  // endregion

  // region Organization normalizers

  @Test fun `orga name = orga name + orga name`() {
    assertThat(Normalizers.OrganizationName)
      .consistsOf(Validators.OrganizationName, Cleaners.OrganizationName)
  }

  @Test fun `orga street = orga street + orga street`() {
    assertThat(Normalizers.OrganizationStreet)
      .consistsOf(Validators.OrganizationStreet, Cleaners.OrganizationStreet)
  }

  @Test fun `orga postcode = orga postcode + orga postcode`() {
    assertThat(Normalizers.OrganizationPostcode)
      .consistsOf(Validators.OrganizationPostcode, Cleaners.OrganizationPostcode)
  }

  @Test fun `orga city = orga city + orga city`() {
    assertThat(Normalizers.OrganizationCity)
      .consistsOf(Validators.OrganizationCity, Cleaners.OrganizationCity)
  }

  @Test fun `orga country code = orga country code + orga country code`() {
    assertThat(Normalizers.OrganizationCountryCode)
      .consistsOf(Validators.OrganizationCountryCode, Cleaners.OrganizationCountryCode)
  }

  @Test fun `organization = organization + organization`() {
    assertThat(Normalizers.Organization)
      .consistsOf(Validators.Organization, Cleaners.Organization)
  }

  // endregion

  // region Project property normalizers

  @Test fun `project name = project name + project name`() {
    assertThat(Normalizers.PropProjectName)
      .consistsOf(Validators.ProjectName, Cleaners.ProjectName)
  }

  @Test fun `prop project description = not blank nullable + trim nullified`() {
    assertThat(Normalizers.PropProjectDescription)
      .consistsOf(Validators.NotBlank, Cleaners.Trim)
  }

  @Test fun `prop project logo url = url + url`() {
    assertThat(Normalizers.PropProjectLogoUrl)
      .consistsOf(Validators.Url, Cleaners.Url)
  }

  @Test fun `prop project website = url + url`() {
    assertThat(Normalizers.PropProjectWebsite)
      .consistsOf(Validators.Url, Cleaners.Url)
  }

  @Test fun `prop max users = positive long as string + long to cardinal as string`() {
    assertThat(Normalizers.PropMaxUsers)
      .consistsOf(Validators.PositiveLongAsString, Cleaners.LongToCardinalAsString)
  }

  @Test fun `prop on hold = flag as string + flag def true as string`() {
    assertThat(Normalizers.PropOnHold)
      .consistsOf(Validators.FlagAsString, Cleaners.FlagDefTrueAsString)
  }

  // endregion

  // region Other normalizers

  @Test fun `origin = origin + origin`() {
    assertThat(Normalizers.Origin)
      .consistsOf(Validators.Origin, Cleaners.Origin)
  }

  @Test fun `ip address = ip address + ip address`() {
    assertThat(Normalizers.IpAddress)
      .consistsOf(Validators.IpAddress, Cleaners.IpAddress)
  }

  @Test fun `birthday = birthday + birthday`() {
    assertThat(Normalizers.BDay)
      .consistsOf(Validators.BDay, Cleaners.BDay)
  }

  // endregion

  // region Helpers

  // doing a shallow comparison because it's just config testing
  private fun Assert<Normalizer<*>>.consistsOf(validator: Validator<*>, cleaner: Cleaner<*, *>) =
    transform { it.name }.isEqualTo("${validator.name}.${cleaner.name}")

  // endregion

}
