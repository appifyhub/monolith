package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.user.SignupCodeDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface SignupCodeDao : CrudRepository<SignupCodeDbm, String> {

  fun findAllByOwner(owner: UserDbm): List<SignupCodeDbm>

}
