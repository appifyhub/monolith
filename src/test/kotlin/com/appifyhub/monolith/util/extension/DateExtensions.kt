package com.appifyhub.monolith.util.extension

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalUnit
import java.util.Date

fun Date.truncateTo(unit: TemporalUnit): Date = Date(
  Instant.ofEpochMilli(time)
    .atZone(ZoneId.of("UTC"))
    .truncatedTo(unit)
    .toInstant()
    .toEpochMilli()
)
