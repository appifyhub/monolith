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
  private var controlledTime: Long? = 0L

  @Synchronized
  fun set(newTime: Long?) {
    controlledTime = newTime
  }

  override val currentMillis: Long
    @Synchronized get() = controlledTime ?: fakeTime++

  override val currentCalendar: Calendar
    @Synchronized get() = Calendar.getInstance().apply {
      timeInMillis = currentMillis
      timeZone = TimeZone.getTimeZone("UTC")
    }

  override val currentInstant: Instant
    @Synchronized get() = Instant.ofEpochMilli(currentMillis)

  override val currentDate: Date
    @Synchronized get() = Date(currentMillis)

}