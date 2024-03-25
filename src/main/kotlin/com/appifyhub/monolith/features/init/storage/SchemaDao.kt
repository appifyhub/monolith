package com.appifyhub.monolith.features.init.storage

import com.appifyhub.monolith.features.init.storage.model.SchemaDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface SchemaDao : CrudRepository<SchemaDbm, Long>
