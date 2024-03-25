package com.appifyhub.monolith.features.auth.storage

import com.appifyhub.monolith.features.auth.storage.model.TokenDetailsDbm
import com.appifyhub.monolith.features.user.storage.model.UserDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
interface TokenDetailsDao : CrudRepository<TokenDetailsDbm, String> {

  fun findAllByOwnerAndBlocked(owner: UserDbm, blocked: Boolean): List<TokenDetailsDbm>

  fun findAllByOwner(owner: UserDbm): List<TokenDetailsDbm>

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByOwner(owner: UserDbm)

}
