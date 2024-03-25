package com.appifyhub.monolith.features.creator.storage

import com.appifyhub.monolith.features.creator.storage.model.ProjectDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface ProjectDao : CrudRepository<ProjectDbm, Long>
