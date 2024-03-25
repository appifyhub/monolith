package com.appifyhub.monolith.features.user.api

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.text.ParseException
import java.util.Calendar
import java.util.TimeZone

class DateTimeMapperTest {

  private val dateString = "1234-05-06"
  private val dateTimeString = "1234-05-06 07:08"

  private val dateCalendar = Calendar.getInstance().apply {
    timeZone = TimeZone.getTimeZone("UTC")
    set(Calendar.YEAR, 1234)
    set(Calendar.MONTH, Calendar.MAY) // 5
    set(Calendar.DAY_OF_MONTH, 6)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
  }
  private val dateTimeCalendar = (dateCalendar.clone() as Calendar).apply {
    set(Calendar.HOUR_OF_DAY, 7)
    set(Calendar.MINUTE, 8)
  }

  @Test fun `formatting as simple date works`() {
    assertThat(DateTimeMapper.formatAsDate(dateCalendar.time))
      .isEqualTo(dateString)
  }

  @Test fun `formatting as simple date & time works`() {
    assertThat(DateTimeMapper.formatAsDateTime(dateTimeCalendar.time))
      .isEqualTo(dateTimeString)
  }

  @Test fun `parsing invalid date throws`() {
    assertFailure { DateTimeMapper.parseAsDate("invalid") }
      .hasClass(ParseException::class)
  }

  @Test fun `parsing a simple date works`() {
    assertThat(DateTimeMapper.parseAsDate(dateString))
      .isEqualTo(dateCalendar.time)
  }

  @Test fun `parsing invalid datetime throws`() {
    assertFailure { DateTimeMapper.parseAsDateTime("invalid") }
      .hasClass(ParseException::class)
  }

  @Test fun `parsing a simple datetime works`() {
    assertThat(DateTimeMapper.parseAsDateTime(dateTimeString))
      .isEqualTo(dateTimeCalendar.time)
  }

}
