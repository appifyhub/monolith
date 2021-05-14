package com.appifyhub.monolith.util

import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import org.springframework.stereotype.Component

@Component(TimeProvider.SYSTEM)
class TimeProviderSystem : TimeProvider {

  override val currentMillis: Long
    get() = System.currentTimeMillis()

  override val currentCalendar: Calendar
    get() = Calendar.getInstance().apply {
      timeZone = TimeZone.getTimeZone("UTC")
      time = currentDate
    }

  override val currentInstant: Instant
    get() = Instant.ofEpochMilli(currentMillis)

  override val currentDate: Date
    get() = Date(currentMillis)

  override fun toString() = "System Time [$currentDate]"

}
