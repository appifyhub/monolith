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
object TimeProviderFake : TimeProvider {

  private var fakeTime = 0L

  fun set(newTime: Long) {
    fakeTime = newTime
  }

  override val currentMillis: Long
    get() = fakeTime

  override val currentCalendar: Calendar
    get() = Calendar.getInstance().apply {
      timeInMillis = currentMillis
      timeZone = TimeZone.getTimeZone("UTC")
    }

  override val currentInstant: Instant
    get() = Instant.ofEpochMilli(currentMillis)

  override val currentDate: Date
    get() = Date(currentMillis)

}