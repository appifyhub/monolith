package com.appifyhub.monolith.features.init.domain.service

import com.appifyhub.monolith.features.init.domain.model.Schema

interface SchemaService {

  @Throws fun update(schema: Schema)

  fun isInitialized(version: Long): Boolean

}
