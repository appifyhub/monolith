package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
@Suppress("FunctionName")
interface UserDao : CrudRepository<UserDbm, UserIdDbm> {

  fun findAllByProject_ProjectId(projectId: Long): List<UserDbm>

  fun findByIdAndVerificationToken(userId: UserIdDbm, verificationToken: String): UserDbm

  fun searchAllByProject_ProjectIdAndNameLike(projectId: Long, name: String): List<UserDbm>

  fun searchAllByProject_ProjectIdAndContactLike(projectId: Long, contact: String): List<UserDbm>

  fun countAllByProject_ProjectId(projectId: Long): Long

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByProject_ProjectId(projectId: Long)

}
