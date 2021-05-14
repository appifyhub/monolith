package com.appifyhub.monolith.util.ext

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.network.user.DateTimeMapper
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import org.junit.jupiter.api.Test

class DateExtensionsTest {

  @Test fun `date is truncated correctly to seconds`() {
    val original = Date(
      ZonedDateTime.of(2134, 5, 6, 7, 8, 9, 10, ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
    )

    assertThat(original.truncateTo(ChronoUnit.MINUTES))
      .isEqualTo(DateTimeMapper.parseAsDateTime("2134-05-06 07:08"))
  }

  @Test fun `date is truncated correctly to days`() {
    val original = Date(
      ZonedDateTime.of(2134, 5, 6, 7, 8, 9, 10, ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
    )

    assertThat(original.truncateTo(ChronoUnit.DAYS))
      .isEqualTo(DateTimeMapper.parseAsDate("2134-05-06"))
  }

}
