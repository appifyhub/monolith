package com.appifyhub.monolith.features.init.repository

import com.appifyhub.monolith.features.init.domain.model.Schema

interface SchemaRepository {

  @Throws fun save(schema: Schema)

  fun isInitialized(version: Long): Boolean

}
