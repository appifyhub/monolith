package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.auth.TokenDetailsDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface TokenDetailsDao : CrudRepository<TokenDetailsDbm, String> {

  fun findAllByOwnerAndBlocked(owner: UserDbm, blocked: Boolean): List<TokenDetailsDbm>

  fun findAllByOwner(owner: UserDbm): List<TokenDetailsDbm>

}
