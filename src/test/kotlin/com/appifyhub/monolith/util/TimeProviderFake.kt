package com.appifyhub.monolith.util

import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.network.user.DateTimeMapper
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Duration
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
  var staticTime: () -> Long? = { null },
) : TimeProvider {

  override val currentMillis: Long
    @Synchronized get() = staticTime() ?: timeIncrement()

  override val currentCalendar: Calendar
    @Synchronized get() = Calendar.getInstance().apply {
      timeInMillis = currentMillis
      timeZone = TimeZone.getTimeZone("UTC")
    }

  override val currentInstant: Instant
    @Synchronized get() = Instant.ofEpochMilli(currentMillis)

  override val currentDate: Date
    @Synchronized get() = Date(currentMillis)

  override fun toString(): String {
    // make sure not to increment for printing purposes
    val timestamp = staticTime() ?: incrementalTime
    val dateTime = DateTimeMapper.formatAsDateTime(Date(timestamp))
    return "Fake Time [$dateTime]. " +
      "Time = $incrementalTime, " +
      "Increment = $timeIncrement, " +
      "Static = ${staticTime()}"
  }

  fun advanceBy(amount: Duration) = staticTime()?.let {
    staticTime = { it + amount.toMillis() }
  } ?: run {
    incrementalTime += amount.toMillis()
  }

  fun reverseBy(amount: Duration) = staticTime()?.let {
    staticTime = { it - amount.toMillis() }
  } ?: run {
    incrementalTime -= amount.toMillis()
  }

  private fun timeIncrement(): Long {
    val result = incrementalTime
    incrementalTime += timeIncrement
    return result
  }

}
