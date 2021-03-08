package com.appifyhub.monolith.service.validation

import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.util.ext.empty
import com.appifyhub.monolith.util.ext.takeIfNotBlank

object Cleaners {

  // Generic cleaners

  val Trim = cleansToNonNull<String> { it?.trim().orEmpty() }
  val TrimNullable = cleansToNullable<String> { it?.trim() }
  val RemoveSpaces = cleansToNonNull<String> { it?.filter { char -> !char.isWhitespace() }.orEmpty() }
  val RemoveSpacesNullable = cleansToNullable<String> { it?.filter { char -> !char.isWhitespace() } }
  val LongToCardinal = cleansToNonNull<Long> { it?.takeIf { num -> num > 0L } ?: 0L }

  // Top level cleaners

  val ProjectId = LongToCardinal
  val AccountId = LongToCardinal
  val ProjectName = Trim

  // ID domain cleaners

  val CustomUserId = RemoveSpaces
  val Username = RemoveSpaces
  val RawSignature = Trim
  val UserId = cleansToNonNull<UserId> { UserId(RemoveSpaces.clean(it?.id), LongToCardinal.clean(it?.projectId)) }

  // Contact cleaners

  val Name = TrimNullable
  val CustomContact = TrimNullable
  val Email = RemoveSpaces
  val Phone = cleansToNonNull<String> cleaner@{ phone ->
    var result = phone?.trim()
    if (result.isNullOrBlank()) return@cleaner String.empty
    // remove non-numeric chars
    result = result.filter { it.isDigit() }
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
  val OrganizationCountryCode = cleansToNullable<String> { it?.trim()?.takeIfNotBlank()?.take(2)?.toUpperCase() }
  val Organization = cleansToNullable<Organization> {
    it?.copy(
      name = OrganizationName.clean(it.name),
      street = OrganizationStreet.clean(it.street),
      postcode = OrganizationPostcode.clean(it.postcode),
      city = OrganizationCity.clean(it.city),
      countryCode = OrganizationCountryCode.clean(it.countryCode),
    ).takeIf { orga ->
      // need at least one property set
      !orga?.name.isNullOrEmpty()
        || !orga?.street.isNullOrEmpty()
        || !orga?.postcode.isNullOrEmpty()
        || !orga?.city.isNullOrEmpty()
        || !orga?.countryCode.isNullOrEmpty()
    }
  }

  // Other cleaners

  val Origin = TrimNullable

  val BDay = cleansToNullable<BDay> { it }

}