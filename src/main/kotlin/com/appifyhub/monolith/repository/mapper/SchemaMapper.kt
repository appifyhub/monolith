package com.appifyhub.monolith.repository.mapper

import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.storage.model.schema.SchemaDbm

fun SchemaDbm.toDomain(): Schema = Schema(
  version = version,
  isInitialized = isInitialized,
)

fun Schema.toData(): SchemaDbm = SchemaDbm(
  version = version,
  isInitialized = isInitialized,
)