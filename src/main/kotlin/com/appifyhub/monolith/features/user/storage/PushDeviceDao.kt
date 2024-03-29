package com.appifyhub.monolith.features.user.storage

import com.appifyhub.monolith.features.user.storage.model.PushDeviceDbm
import com.appifyhub.monolith.features.user.storage.model.UserDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
interface PushDeviceDao : CrudRepository<PushDeviceDbm, String> {

  fun findAllByOwner(owner: UserDbm): List<PushDeviceDbm>

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByOwner(owner: UserDbm)

}
