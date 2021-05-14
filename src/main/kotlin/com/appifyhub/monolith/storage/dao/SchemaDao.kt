package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.schema.SchemaDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface SchemaDao : CrudRepository<SchemaDbm, Long>
