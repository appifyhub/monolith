package com.appifyhub.monolith.domain.mapper

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.appifyhub.monolith.domain.geo.Geolocation
import com.appifyhub.monolith.util.Stubs
import com.ip2location.IPResult
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test

class GeoMapperTest {

  @Test fun `IP result data to geolocation domain (all filled)`() {
    val result = mock<IPResult> {
      on { countryShort } doReturn Stubs.geo.countryCode
      on { countryLong } doReturn Stubs.geo.countryName
      on { region } doReturn Stubs.geo.region
      on { city } doReturn Stubs.geo.city
    }

    assertThat(result.toDomain())
      .isNotNull()
      .isDataClassEqualTo(Stubs.geo)
  }

  @Test fun `IP result data to geolocation domain (some filled)`() {
    val result = mock<IPResult> {
      on { countryShort } doReturn "\t \n"
      on { countryLong } doReturn "-"
      on { region } doReturn null
      on { city } doReturn Stubs.geo.city
    }

    assertThat(result.toDomain())
      .isNotNull()
      .isDataClassEqualTo(
        Geolocation(
          countryCode = null,
          countryName = null,
          region = null,
          city = Stubs.geo.city,
        )
      )
  }

  @Test fun `IP result data to geolocation domain (none filled)`() {
    val result = mock<IPResult> {
      on { countryShort } doReturn null
      on { countryLong } doReturn null
      on { region } doReturn null
      on { city } doReturn null
    }

    assertThat(result.toDomain())
      .isNull()
  }

  @Test fun `geolocation domain to merged string (all filled)`() {
    val geo = Stubs.geo

    assertThat(geo.mergeToString())
      .isEqualTo(Stubs.geoMerged)
  }

  @Test fun `geolocation domain to merged string (some filled)`() {
    val geo = Geolocation(
      countryCode = null,
      countryName = Stubs.geo.countryName,
      region = null,
      city = Stubs.geo.city,
    )

    assertThat(geo.mergeToString())
      .isEqualTo("${Stubs.geo.countryName}, ${Stubs.geo.city}")
  }

  @Test fun `geolocation domain to merged string (none filled)`() {
    val geo = Geolocation(
      countryCode = null,
      countryName = null,
      region = null,
      city = null,
    )

    assertThat(geo.mergeToString())
      .isEqualTo("")
  }

}
