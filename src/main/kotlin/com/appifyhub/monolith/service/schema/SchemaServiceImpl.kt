package com.appifyhub.monolith.service.schema

import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.repository.schema.SchemaRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SchemaServiceImpl(
  private val schemaRepository: SchemaRepository,
) : SchemaService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun update(schema: Schema) {
    log.debug("Updating schema $schema")
    // TODO MM validation missing
    schemaRepository.update(schema)
  }

  override fun isInitialized(version: Long): Boolean {
    log.debug("Checking if schema initialized v$version")
    // TODO MM validation missing
    return schemaRepository.isInitialized(version)
  }

}