package com.appifyhub.monolith.service.schema

import com.appifyhub.monolith.domain.schema.Schema

interface SchemaService {

  @Throws fun update(schema: Schema)

  fun isInitialized(version: Long): Boolean

}
