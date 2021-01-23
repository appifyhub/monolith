package com.appifyhub.monolith.network.user

import java.text.SimpleDateFormat
import java.util.Date

object DateTimeMapper {

  object Format {
    object Network {
      const val DATE = "yyyy-MM-dd"
      const val DATE_TIME = "yyyy-MM-dd HH:mm"
    }
  }

  private val simpleDateFormat = SimpleDateFormat(Format.Network.DATE)
  private val simpleDateTimeFormat = SimpleDateFormat(Format.Network.DATE_TIME)

  fun formatAsDate(date: Date): String = simpleDateFormat.format(date)
  fun formatAsDateTime(datetime: Date): String = simpleDateTimeFormat.format(datetime)

  @Throws
  fun parseAsDate(source: String): Date = simpleDateFormat.parse(source)

}