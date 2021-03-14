package com.appifyhub.monolith.service.validation

import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.util.ext.hasNoSpaces
import com.appifyhub.monolith.util.ext.isNullOrNotBlank
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE

object Validators {

  private const val AGE_MAX = 100L
  private const val AGE_MIN = 10L

  private val REGEX_EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,10}$", CASE_INSENSITIVE)

  private val phoneNumberUtil = PhoneNumberUtil.getInstance()
  private val log = LoggerFactory.getLogger(this::class.java)

  // Generic validators

  val NotBlank = validatesAs<String>("NotBlank") { !it.isNullOrBlank() }
  val NotBlankNullable = validatesAs<String>("NotBlankNullable") { it.isNullOrNotBlank() }
  val NoSpaces = validatesAs<String>("NoSpaces") { !it.isNullOrEmpty() && it.hasNoSpaces() }
  val NoSpacesNullable = validatesAs<String>("NoSpacesNullable") {
    it == null || (it.isNotBlank() && it.hasNoSpaces())
  }
  val PositiveLong = validatesAs<Long>("PositiveLong") { it != null && it > 0L }

  // Top level domain validators

  val ProjectId = PositiveLong
  val AccountId = PositiveLong
  val ProjectName = NotBlank

  // ID validators

  val CustomUserId = NoSpaces
  val Username = NoSpaces
  val RawSignature = NotBlank
  val RawSignatureNullable = NotBlankNullable
  val UserId = validatesAs<UserId>("UserId") { NoSpaces.isValid(it?.id) && PositiveLong.isValid(it?.projectId) }

  // Contact validators

  val Name = NotBlankNullable
  val CustomContact = NotBlankNullable
  val Email = validatesAs<String>("Email") { NotBlank.isValid(it) && REGEX_EMAIL.matcher(it!!).matches() }
  val Phone = validatesAs<String>("Phone") validator@{
    if (!NoSpaces.isValid(it)) return@validator false
    try {
      val number = phoneNumberUtil.parse(it, FROM_NUMBER_WITH_PLUS_SIGN.name)
      phoneNumberUtil.isValidNumber(number) // validates international format
    } catch (t: Throwable) {
      log.warn("Failed to parse the number", t)
      false
    }
  }

  // Organization validators

  val OrganizationName = NotBlankNullable
  val OrganizationStreet = NotBlankNullable
  val OrganizationPostcode = NotBlankNullable
  val OrganizationCity = NotBlankNullable
  val OrganizationCountryCode = validatesAs<String>("OrganizationCountryCode") validator@{ code ->
    if (code == null) return@validator true
    if (code.length != 2) return@validator false
    code.all { it.isLetter() && it.isUpperCase() }
  }
  val Organization = validatesAs<Organization>("Organization") validator@{
    if (it == null) return@validator true
    if (!OrganizationName.isValid(it.name)) return@validator false
    if (!OrganizationStreet.isValid(it.street)) return@validator false
    if (!OrganizationPostcode.isValid(it.postcode)) return@validator false
    if (!OrganizationCity.isValid(it.city)) return@validator false
    if (!OrganizationCountryCode.isValid(it.countryCode)) return@validator false
    true
  }

  // Other validators

  val Origin = NotBlankNullable

  val BDay = validatesAs<BDay>("BDay") validator@{
    val rawBirthday = it?.first ?: return@validator true
    val birthday = Timestamp(rawBirthday.time).toLocalDateTime().atZone(ZoneId.of("UTC"))
    val today = Timestamp(it.second.currentMillis).toLocalDateTime().atZone(ZoneId.of("UTC"))
    if (!today.isAfter(birthday)) return@validator false
    val age = ChronoUnit.YEARS.between(birthday, today)
    return@validator age in AGE_MIN..AGE_MAX
  }

}
