package com.appifyhub.monolith.repository.schema

import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.storage.dao.SchemaDao
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class SchemaRepositoryImpl(
  private val schemaDao: SchemaDao,
) : SchemaRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun update(schema: Schema) {
    log.debug("Updating schema $schema")
    val oldValue = schemaDao.findById(schema.version).orElse(null)?.toDomain()
    if (oldValue?.isInitialized == true) {
      throw IllegalArgumentException("Schema v${schema.version} already initialized")
    }
    val newValue = schema.toData()
    schemaDao.save(newValue)
  }

  override fun isInitialized(version: Long): Boolean {
    log.debug("Checking if schema initialized v$version")
    return try {
      schemaDao.findById(version).map { it.isInitialized }.orElse(false)
    } catch (t: Throwable) {
      log.warn("Failed to fetch schema v$version", t)
      false
    }
  }

}