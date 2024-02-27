package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.creator.ProjectCreationDbm
import com.appifyhub.monolith.storage.model.creator.ProjectCreationKeyDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
@Suppress("FunctionName")
interface ProjectCreationDao : CrudRepository<ProjectCreationDbm, ProjectCreationKeyDbm> {

  fun findAllByData_CreatorUserIdAndData_CreatorProjectId(userId: String, projectId: Long): List<ProjectCreationDbm>

  fun findByData_CreatedProjectId(projectId: Long): ProjectCreationDbm

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByData_CreatorUserIdAndData_CreatorProjectId(userId: String, projectId: Long)

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByData_CreatedProjectId(projectId: Long)

}
