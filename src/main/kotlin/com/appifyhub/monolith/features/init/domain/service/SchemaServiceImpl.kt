package com.appifyhub.monolith.features.init.domain.service

import com.appifyhub.monolith.features.init.domain.model.Schema
import com.appifyhub.monolith.features.init.repository.SchemaRepository
import com.appifyhub.monolith.util.extension.requireValid
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SchemaServiceImpl(
  private val schemaRepository: SchemaRepository,
) : SchemaService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun update(schema: Schema) {
    log.debug("Updating schema $schema")
    Normalizers.Cardinal.run(schema.version).requireValid { "Schema Version" }
    schemaRepository.save(schema)
  }

  override fun isInitialized(version: Long): Boolean {
    log.debug("Checking if schema initialized v$version")
    Normalizers.Cardinal.run(version).requireValid { "Schema Version" }
    return schemaRepository.isInitialized(version)
  }

}
