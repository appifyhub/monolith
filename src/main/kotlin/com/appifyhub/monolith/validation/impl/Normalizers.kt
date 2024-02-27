package com.appifyhub.monolith.validation.impl

import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.validation.normalizesNonNull
import com.appifyhub.monolith.validation.normalizesNullable
import java.util.Date

typealias BDay = Pair<Date?, TimeProvider>

object Normalizers {

  // Generic normalizers

  val NotBlank = normalizesNonNull(Validators.NotBlank, Cleaners.Trim)
  val NotBlankNullable = normalizesNullable(Validators.NotBlankNullable, Cleaners.TrimNullable)
  val Dense = normalizesNonNull(Validators.NoSpaces, Cleaners.RemoveSpaces)
  val DenseNullable = normalizesNullable(Validators.NoSpacesNullable, Cleaners.RemoveSpacesNullable)
  val Cardinal = normalizesNonNull(Validators.PositiveLong, Cleaners.LongToCardinal)
  val FlagDefFalse = normalizesNonNull(Validators.Flag, Cleaners.FlagDefFalse)
  val FlagDefTrue = normalizesNonNull(Validators.Flag, Cleaners.FlagDefTrue)

  // ID normalizers

  val CustomUserId = normalizesNonNull(Validators.CustomUserId, Cleaners.CustomUserId)
  val Username = normalizesNonNull(Validators.Username, Cleaners.Username)
  val RawSignature = normalizesNonNull(Validators.RawSignature, Cleaners.RawSignature)
  val RawSignatureNullified = normalizesNullable(Validators.RawSignatureNullable, Cleaners.RawSignatureNullified)
  val UserId = normalizesNonNull(Validators.UserId, Cleaners.UserId)

  // Contact normalizers

  val Name = normalizesNullable(Validators.Name, Cleaners.Name)
  val CustomContact = normalizesNullable(Validators.CustomContact, Cleaners.CustomContact)
  val Email = normalizesNonNull(Validators.Email, Cleaners.Email)
  val Phone = normalizesNonNull(Validators.Phone, Cleaners.Phone)

  // Organization normalizers

  val OrganizationName = normalizesNullable(Validators.OrganizationName, Cleaners.OrganizationName)
  val OrganizationStreet = normalizesNullable(Validators.OrganizationStreet, Cleaners.OrganizationStreet)
  val OrganizationPostcode = normalizesNullable(Validators.OrganizationPostcode, Cleaners.OrganizationPostcode)
  val OrganizationCity = normalizesNullable(Validators.OrganizationCity, Cleaners.OrganizationCity)
  val OrganizationCountryCode = normalizesNullable(Validators.OrganizationCountryCode, Cleaners.OrganizationCountryCode)
  val Organization = normalizesNullable(Validators.Organization, Cleaners.Organization)

  // Project normalizers

  val ProjectId = normalizesNonNull(Validators.ProjectId, Cleaners.ProjectId)
  val ProjectName = normalizesNonNull(Validators.ProjectName, Cleaners.ProjectName)
  val ProjectDescription = normalizesNullable(Validators.NotBlankNullable, Cleaners.TrimNullified)
  val ProjectLogoUrl = normalizesNullable(Validators.UrlNullable, Cleaners.UrlNullified)
  val ProjectWebsiteUrl = normalizesNullable(Validators.UrlNullable, Cleaners.UrlNullified)

  // Other normalizers

  val Origin = normalizesNullable(Validators.Origin, Cleaners.Origin)
  val IpAddress = normalizesNullable(Validators.IpAddress, Cleaners.IpAddress)
  val BDay = normalizesNullable(Validators.BDay, Cleaners.BDay)
  val LanguageTag = normalizesNullable(Validators.LanguageTag, Cleaners.LanguageTagNullified)
  val MessageTemplateId = normalizesNonNull(Validators.MessageTemplateId, Cleaners.MessageTemplateId)
  val MessageTemplateName = normalizesNonNull(Validators.MessageTemplateName, Cleaners.MessageTemplateName)
  val MessageTemplate = normalizesNonNull(Validators.MessageTemplate, Cleaners.MessageTemplate)
  val PushDeviceToken = normalizesNonNull(Validators.PushDeviceToken, Cleaners.PushDeviceToken)
  val SignupCode = normalizesNonNull(Validators.SignupCode, Cleaners.SignupCode)

  // Integrations normalizers

  val MailgunConfigData = normalizesNullable(Validators.MailgunConfigData, Cleaners.MailgunConfigData)
  val TwilioConfigData = normalizesNullable(Validators.TwilioConfigData, Cleaners.TwilioConfigData)
  val FirebaseConfigData = normalizesNullable(Validators.FirebaseConfigData, Cleaners.FirebaseConfigData)

}
