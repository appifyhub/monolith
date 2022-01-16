package com.appifyhub.monolith.repository.messaging

import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.MessageTemplateCreator
import com.appifyhub.monolith.storage.dao.MessageTemplateDao
import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class MessageTemplateRepositoryImpl(
  private val templateDao: MessageTemplateDao,
  private val timeProvider: TimeProvider,
) : MessageTemplateRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addTemplate(templateCreator: MessageTemplateCreator): MessageTemplate {
    log.debug("Adding template $templateCreator")

    return templateDao.save(templateCreator.toData(timeProvider)).toDomain()
  }

  override fun updateTemplate(template: MessageTemplate): MessageTemplate {
    log.debug("Updating template $template")

    return templateDao.save(template.toData()).toDomain()
  }

  override fun fetchTemplateById(id: Long): MessageTemplate {
    log.debug("Fetching template $id")

    return templateDao.findById(id).get().toDomain()
  }

  override fun deleteTemplateById(id: Long) {
    log.debug("Deleting template $id")

    return templateDao.deleteById(id)
  }

  override fun fetchAllTemplatesByProjectId(projectId: Long): List<MessageTemplate> {
    log.debug("Fetching all templates by project ID $projectId")

    return templateDao.findAllByProject_ProjectId(projectId).map(MessageTemplateDbm::toDomain)
  }

  override fun deleteAllTemplatesByProjectId(projectId: Long) {
    log.debug("Deleting all templates by project ID $projectId")

    return templateDao.deleteAllByProject_ProjectId(projectId)
  }

}
