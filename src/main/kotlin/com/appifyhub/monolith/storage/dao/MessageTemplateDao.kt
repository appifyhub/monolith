package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import org.springframework.data.repository.CrudRepository
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.transaction.annotation.Transactional

@RepositoryRestResource
@Suppress("FunctionName")
interface MessageTemplateDao : CrudRepository<MessageTemplateDbm, Long> {

  fun findAllByProject_ProjectId(projectId: Long): List<MessageTemplateDbm>

  fun findAllByProject_ProjectIdAndName(projectId: Long, name: String): List<MessageTemplateDbm>

  fun findAllByProject_ProjectIdAndNameAndLanguageTag(
    projectId: Long,
    name: String,
    languageTag: String,
  ): List<MessageTemplateDbm>

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByProject_ProjectId(projectId: Long)

  @Transactional(rollbackFor = [Exception::class])
  fun deleteAllByProject_ProjectIdAndName(projectId: Long, name: String)

}
