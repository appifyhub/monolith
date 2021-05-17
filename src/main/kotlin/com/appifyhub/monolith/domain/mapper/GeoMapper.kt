package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.geo.Geolocation
import com.ip2location.IPResult

private const val MISSING_PROP = "-"

fun IPResult.toDomain(): Geolocation? = Geolocation(
  countryCode = countryShort.takeIfValid(),
  countryName = countryLong.takeIfValid(),
  region = region.takeIfValid(),
  city = city.takeIfValid(),
).takeIf {
  it.countryCode != null ||
    it.countryName != null ||
    it.region != null ||
    it.city != null
}

private fun String?.takeIfValid() = this?.trim()?.takeIf { it.isNotBlank() && it != MISSING_PROP }
