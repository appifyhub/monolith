package com.appifyhub.monolith.network.user

import java.text.SimpleDateFormat
import java.util.Date

object DateTimeMapper {

  object Format {
    object Network {
      const val DATE = "yyyy-MM-dd"
    }
  }

  private val simpleDateFormat = SimpleDateFormat(Format.Network.DATE)

  fun formatAsDate(date: Date): String = simpleDateFormat.format(date)

  @Throws
  fun parseAsDate(source: String): Date = simpleDateFormat.parse(source)

}