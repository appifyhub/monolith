package com.appifyhub.monolith.util

import com.appifyhub.monolith.TestAppifyHubApplication
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Primary
@Component
@Profile(TestAppifyHubApplication.PROFILE)
class TimeProviderFake(
  var timeIncrement: Long = 100L,
  var incrementalTime: Long = 0L,
  var staticTime: Long? = null,
) : TimeProvider {

  override val currentMillis: Long
    @Synchronized get() = staticTime ?: timeIncrement()

  override val currentCalendar: Calendar
    @Synchronized get() = Calendar.getInstance().apply {
      timeInMillis = currentMillis
      timeZone = TimeZone.getTimeZone("UTC")
    }

  override val currentInstant: Instant
    @Synchronized get() = Instant.ofEpochMilli(currentMillis)

  override val currentDate: Date
    @Synchronized get() = Date(currentMillis)

  private fun timeIncrement(): Long {
    val result = incrementalTime
    incrementalTime += timeIncrement
    return result
  }

}