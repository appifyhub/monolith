package com.appifyhub.monolith.features.init.domain

import com.appifyhub.monolith.features.init.domain.model.Schema
import com.appifyhub.monolith.features.init.storage.model.SchemaDbm

fun SchemaDbm.toDomain(): Schema = Schema(
  version = version,
  isInitialized = isInitialized,
)

fun Schema.toData(): SchemaDbm = SchemaDbm(
  version = version,
  isInitialized = isInitialized,
)
