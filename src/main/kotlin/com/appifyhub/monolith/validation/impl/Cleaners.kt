package com.appifyhub.monolith.validation.impl

import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.util.ext.takeIfNotBlank
import com.appifyhub.monolith.validation.cleansToNonNull
import com.appifyhub.monolith.validation.cleansToNullable
import java.util.Locale

object Cleaners {

  // Generic cleaners

  val Trim = cleansToNonNull<String>("Trim") { it?.trim().orEmpty() }
  val TrimNullable = cleansToNullable<String>("TrimNullable") { it?.trim() }
  val TrimNullified = cleansToNullable<String>("TrimNullified") { it?.trim()?.takeIfNotBlank() }
  val RemoveSpaces = cleansToNonNull<String>("RemoveSpaces") {
    it?.filter { char -> !char.isWhitespace() }.orEmpty()
  }
  val RemoveSpacesNullable = cleansToNullable<String>("RemoveSpacesNullable") {
    it?.filter { char -> !char.isWhitespace() }
  }
  val LongToCardinal = cleansToNonNull<Long>("LongToCardinal") { it?.takeIf { num -> num > 0L } ?: 0L }
  val FlagDefFalse = cleansToNonNull<Boolean>("FlagDefaultFalse") { it ?: false }
  val FlagDefTrue = cleansToNonNull<Boolean>("FlagDefaultTrue") { it ?: true }

  // ID domain cleaners

  val CustomUserId = RemoveSpaces
  val Username = RemoveSpaces
  val RawSignature = Trim
  val RawSignatureNullified = TrimNullified
  val UserId = cleansToNonNull<UserId>("UserId") {
    UserId(
      userId = RemoveSpaces.clean(it?.userId),
      projectId = LongToCardinal.clean(it?.projectId),
    )
  }

  // Contact cleaners

  val Name = TrimNullable
  val CustomContact = TrimNullable
  val Email = RemoveSpaces
  val Phone = cleansToNonNull<String>("Phone") cleaner@{ phone ->
    var result = phone?.trim()
    if (result.isNullOrBlank()) return@cleaner ""
    // remove non-numeric chars
    result = result.filter { it.isDigit() }
    if (result.length < 3) return@cleaner ""
    // 0123456789 (local) -> make it international
    if (result[0] == '0' && result[1] != '0') result = "0$result"
    // 00123456789 (international) -> convert to plus format
    if (result.startsWith("00")) result = result.replaceFirst("00", "")
    // 123456789 (international, no zeros) -> add the plus in front
    "+$result"
  }

  // Organization cleaners

  val OrganizationName = TrimNullable
  val OrganizationStreet = TrimNullable
  val OrganizationPostcode = TrimNullable
  val OrganizationCity = TrimNullable
  val OrganizationCountryCode = cleansToNullable<String>("OrganizationCountryCode") {
    it?.trim()?.takeIfNotBlank()?.take(2)?.uppercase()
  }
  val Organization = cleansToNullable<Organization>("Organization") {
    it?.copy(
      name = OrganizationName.clean(it.name),
      street = OrganizationStreet.clean(it.street),
      postcode = OrganizationPostcode.clean(it.postcode),
      city = OrganizationCity.clean(it.city),
      countryCode = OrganizationCountryCode.clean(it.countryCode),
    ).takeIf { orga ->
      // need at least one property set
      !orga?.name.isNullOrEmpty() ||
        !orga?.street.isNullOrEmpty() ||
        !orga?.postcode.isNullOrEmpty() ||
        !orga?.city.isNullOrEmpty() ||
        !orga?.countryCode.isNullOrEmpty()
    }
  }

  // Project cleaners

  val ProjectId = LongToCardinal
  val ProjectName = Trim
  val Url = Trim
  val UrlNullified = TrimNullified

  // Other cleaners

  val Origin = TrimNullable
  val IpAddress = RemoveSpacesNullable
  val BDay = cleansToNullable<BDay>("BDay") { it }
  val LanguageTagNullified = cleansToNullable<String>("LanguageTagNullified") {
    if (it == null) return@cleansToNullable null
    Locale.forLanguageTag(it.trim()).toLanguageTag()
      ?.takeIf { tag -> tag.isNotBlank() && tag != "und" }
  }

}
