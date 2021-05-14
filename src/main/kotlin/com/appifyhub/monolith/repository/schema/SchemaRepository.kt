package com.appifyhub.monolith.repository.schema

import com.appifyhub.monolith.domain.schema.Schema

interface SchemaRepository {

  @Throws fun update(schema: Schema)

  fun isInitialized(version: Long): Boolean

}
