package com.appifyhub.monolith.features.user.storage

import com.appifyhub.monolith.features.user.storage.model.SignupCodeDbm
import com.appifyhub.monolith.features.user.storage.model.UserDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
interface SignupCodeDao : CrudRepository<SignupCodeDbm, String> {

  fun findAllByOwner(owner: UserDbm): List<SignupCodeDbm>

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByOwner(owner: UserDbm)

}
