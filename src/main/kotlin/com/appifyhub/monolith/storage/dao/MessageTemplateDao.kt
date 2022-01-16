package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
@Suppress("FunctionName")
interface MessageTemplateDao : CrudRepository<MessageTemplateDbm, Long> {

  fun findAllByProject_ProjectId(id: Long): List<MessageTemplateDbm>

  @Transactional
  fun deleteAllByProject_ProjectId(id: Long)

}
