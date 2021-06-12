package com.appifyhub.monolith.domain.admin.ops

import com.appifyhub.monolith.domain.admin.property.Property

data class PropertyMultiUpdater(
  val updatedProperties: List<Property<*>>,
  val clearedProperties: List<Property<*>>,
)
