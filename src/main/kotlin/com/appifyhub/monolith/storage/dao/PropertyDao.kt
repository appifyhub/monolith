package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.admin.PropertyDbm
import com.appifyhub.monolith.storage.model.admin.PropertyIdDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
@Suppress("FunctionName")
interface PropertyDao : CrudRepository<PropertyDbm, PropertyIdDbm> {

  fun findAllByProject_ProjectId(projectId: Long): List<PropertyDbm>

  fun findAllById_Name(name: String): List<PropertyDbm>

  @Transactional
  fun deleteAllByProject_ProjectId(projectId: Long)

}
