package com.appifyhub.monolith.domain.admin.property

import java.util.Date

sealed interface Property<out T : Any> {

  companion object // for extensions

  // Shared properties

  val config: PropertyConfiguration
  val projectId: Long
  val rawValue: String
  val updatedAt: Date

  @Throws fun typed(): T

  // Implementations

  data class StringProp(
    override val config: PropertyConfiguration,
    override val projectId: Long,
    override val rawValue: String,
    override val updatedAt: Date,
  ) : Property<String> {
    override fun typed() = rawValue
  }

  data class IntegerProp(
    override val config: PropertyConfiguration,
    override val projectId: Long,
    override val rawValue: String,
    override val updatedAt: Date,
  ) : Property<Int> {
    override fun typed() = rawValue.toInt()
  }

  data class DecimalProp(
    override val config: PropertyConfiguration,
    override val projectId: Long,
    override val rawValue: String,
    override val updatedAt: Date,
  ) : Property<Double> {
    override fun typed() = rawValue.toDouble()
  }

  data class FlagProp(
    override val config: PropertyConfiguration,
    override val projectId: Long,
    override val rawValue: String,
    override val updatedAt: Date,
  ) : Property<Boolean> {
    override fun typed() = rawValue.toBooleanStrict()
  }

}
