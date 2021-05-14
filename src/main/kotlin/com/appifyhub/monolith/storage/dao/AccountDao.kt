package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.admin.AccountDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface AccountDao : CrudRepository<AccountDbm, Long>
