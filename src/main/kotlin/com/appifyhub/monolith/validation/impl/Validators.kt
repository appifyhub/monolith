package com.appifyhub.monolith.validation.impl

import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.util.ext.hasNoSpaces
import com.appifyhub.monolith.util.ext.isNullOrNotBlank
import com.appifyhub.monolith.validation.validatesAs
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN
import java.sql.Timestamp
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE
import org.slf4j.LoggerFactory

object Validators {

  private const val AGE_MAX = 100L
  private const val AGE_MIN = 10L

  // @formatter:off
  private val REGEX_EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,10}$", CASE_INSENSITIVE) // ktlint-disable max-line-length
  private val REGEX_IP_4 = Pattern.compile("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$") // ktlint-disable max-line-length
  private val REGEX_IP_6 = Pattern.compile("(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))") // ktlint-disable max-line-length
  private val REGEX_URL = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&$@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&$@#/%=~_|]") // ktlint-disable max-line-length
  // @formatter:on

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

  // ID validators

  val CustomUserId = NoSpaces
  val Username = NoSpaces
  val RawSignature = NotBlank
  val RawSignatureNullable = NotBlankNullable
  val UserId = validatesAs<UserId>("UserId") { NoSpaces.isValid(it?.userId) && PositiveLong.isValid(it?.projectId) }

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

  // Project property validators

  val ProjectName = NotBlank
  val Url = validatesAs<String>("Url") { NotBlank.isValid(it) && REGEX_URL.matcher(it!!).matches() }
  val UrlNullable = validatesAs<String>("UrlNullable") {
    if (it == null) return@validatesAs true
    NotBlank.isValid(it) && REGEX_URL.matcher(it).matches()
  }
  val PositiveLongAsString = validatesAs<String>("PositiveLongAsString") {
    it != null && (it.toLongOrNull() ?: 0L) > 0L
  }

  // Other validators

  val Origin = NotBlankNullable

  val IpAddress = validatesAs<String>("IpAddress") {
    if (it == null) return@validatesAs true
    NotBlank.isValid(it) && (REGEX_IP_4.matcher(it).matches() || REGEX_IP_6.matcher(it).matches())
  }

  val BDay = validatesAs<BDay>("BDay") validator@{
    val rawBirthday = it?.first ?: return@validator true
    val birthday = Timestamp(rawBirthday.time).toLocalDateTime().atZone(ZoneId.of("UTC"))
    val today = Timestamp(it.second.currentMillis).toLocalDateTime().atZone(ZoneId.of("UTC"))
    if (!today.isAfter(birthday)) return@validator false
    val age = ChronoUnit.YEARS.between(birthday, today)
    return@validator age in AGE_MIN..AGE_MAX
  }

}
