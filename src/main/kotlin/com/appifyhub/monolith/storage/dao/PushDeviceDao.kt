package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.messaging.PushDeviceDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
interface PushDeviceDao : CrudRepository<PushDeviceDbm, String> {

  fun findAllByOwner(owner: UserDbm): List<PushDeviceDbm>

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByOwner(owner: UserDbm)

}
