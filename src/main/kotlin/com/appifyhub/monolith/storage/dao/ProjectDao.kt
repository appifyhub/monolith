package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import java.util.Optional

@RepositoryRestResource
interface ProjectDao : CrudRepository<ProjectDbm, Long> {

  fun findBySignature(signature: String): Optional<ProjectDbm>

  fun findAllByAccount(account: AccountDbm): List<ProjectDbm>

  fun deleteAllByAccount(account: AccountDbm)

  fun deleteBySignature(signature: String)

}