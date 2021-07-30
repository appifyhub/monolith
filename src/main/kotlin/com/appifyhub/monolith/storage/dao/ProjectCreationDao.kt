package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.admin.ProjectCreationDbm
import com.appifyhub.monolith.storage.model.admin.ProjectCreationKeyDbm
import javax.transaction.Transactional
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
@Suppress("FunctionName")
interface ProjectCreationDao : CrudRepository<ProjectCreationDbm, ProjectCreationKeyDbm> {

  fun findAllByData_CreatorUserIdAndData_CreatorProjectId(userId: String, projectId: Long): List<ProjectCreationDbm>

  fun findByData_CreatedProjectId(projectId: Long): ProjectCreationDbm

  @Transactional
  fun deleteAllByData_CreatorUserIdAndData_CreatorProjectId(userId: String, projectId: Long)

  @Transactional
  fun deleteAllByData_CreatedProjectId(projectId: Long)

}
