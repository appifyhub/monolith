package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.auth.OwnedTokenDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface OwnedTokenDao : CrudRepository<OwnedTokenDbm, String> {

  fun findAllByOwnerAndBlocked(owner: UserDbm, blocked: Boolean): List<OwnedTokenDbm>

  fun findAllByOwner(owner: UserDbm): List<OwnedTokenDbm>

}