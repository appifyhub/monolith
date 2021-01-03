package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
@Suppress("FunctionName")
interface UserDao : CrudRepository<UserDbm, UserIdDbm> {

  fun findAllByProject_ProjectId(projectId: Long): List<UserDbm>

  fun findAllByAccount(account: AccountDbm): List<UserDbm>

  fun findAllByContact(contact: String): List<UserDbm>

  fun deleteAllByProject_ProjectId(projectId: Long)

}