package com.appifyhub.monolith.service.schema

import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.repository.schema.SchemaRepository
import com.appifyhub.monolith.util.ext.requireValid
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
