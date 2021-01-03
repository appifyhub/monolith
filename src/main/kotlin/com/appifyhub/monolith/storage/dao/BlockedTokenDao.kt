package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.auth.BlockedTokenDbm
import com.appifyhub.monolith.storage.model.auth.TokenDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource

@RepositoryRestResource
interface BlockedTokenDao : CrudRepository<BlockedTokenDbm, TokenDbm>