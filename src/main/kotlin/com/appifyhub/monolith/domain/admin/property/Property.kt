package com.appifyhub.monolith.domain.admin.property

import com.appifyhub.monolith.util.ext.silent
import java.util.Date

sealed interface Property<out T : Any> {

  // Shared properties

  val config: PropertyConfiguration
  val projectId: Long
  val rawValue: String?
  val createdAt: Date
  val updatedAt: Date

  fun hasValue(): Boolean = silent { requireValue() } != null
  fun requireValue(): T

  // Implementations

  data class StringProp(
    override val config: PropertyConfiguration,
    override val projectId: Long,
    override val rawValue: String?,
    override val createdAt: Date,
    override val updatedAt: Date = createdAt,
  ) : Property<String> {
    override fun requireValue() = rawValue ?: error("No value found")
  }

  data class IntegerProp(
    override val config: PropertyConfiguration,
    override val projectId: Long,
    override val rawValue: String?,
    override val createdAt: Date,
    override val updatedAt: Date = createdAt,
  ) : Property<Int> {
    override fun requireValue() = rawValue?.toInt() ?: error("No value found")
  }

  data class DecimalProp(
    override val config: PropertyConfiguration,
    override val projectId: Long,
    override val rawValue: String?,
    override val createdAt: Date,
    override val updatedAt: Date = createdAt,
  ) : Property<Double> {
    override fun requireValue() = rawValue?.toDouble() ?: error("No value found")
  }

  data class FlagProp(
    override val config: PropertyConfiguration,
    override val projectId: Long,
    override val rawValue: String?,
    override val createdAt: Date,
    override val updatedAt: Date = createdAt,
  ) : Property<Boolean> {
    override fun requireValue() = rawValue?.toBooleanStrict() ?: error("No value found")
  }

}
