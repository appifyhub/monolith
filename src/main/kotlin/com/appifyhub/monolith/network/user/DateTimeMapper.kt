package com.appifyhub.monolith.network.user

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

object DateTimeMapper {

  object Format {

    object Network {
      const val DATE = "yyyy-MM-dd"
      const val DATE_TIME = "yyyy-MM-dd HH:mm"
    }

  }

  private val simpleDateFormat = SimpleDateFormat(Format.Network.DATE).apply {
    timeZone = TimeZone.getTimeZone("UTC")
  }

  private val simpleDateTimeFormat = SimpleDateFormat(Format.Network.DATE_TIME).apply {
    timeZone = TimeZone.getTimeZone("UTC")
  }

  fun formatAsDate(date: Date): String = simpleDateFormat.format(date)
  fun formatAsDateTime(datetime: Date): String = simpleDateTimeFormat.format(datetime)

  @Throws
  fun parseAsDate(source: String): Date = simpleDateFormat.parse(source)

}