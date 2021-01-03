package com.appifyhub.monolith.util

import java.time.Instant
import java.util.Calendar
import java.util.Date

interface TimeProvider {

  companion object Type {
    const val SYSTEM = "TimeProvider/system"
  }

  val currentMillis: Long
  val currentCalendar: Calendar
  val currentInstant: Instant
  val currentDate: Date

}