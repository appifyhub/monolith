package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface ProjectDao : CrudRepository<ProjectDbm, Long> {

  fun findAllByAccount(account: AccountDbm): List<ProjectDbm>

  fun deleteAllByAccount(account: AccountDbm)

}
