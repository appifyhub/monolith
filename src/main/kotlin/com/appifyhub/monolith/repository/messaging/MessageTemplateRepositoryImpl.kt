package com.appifyhub.monolith.repository.messaging

import com.appifyhub.monolith.domain.mapper.applyTo
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.MessageTemplateCreator
import com.appifyhub.monolith.domain.messaging.ops.MessageTemplateUpdater
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

  override fun fetchTemplateById(id: Long): MessageTemplate {
    log.debug("Fetching template $id")

    return templateDao.findById(id).get().toDomain()
  }

  override fun fetchTemplatesByName(projectId: Long, name: String): List<MessageTemplate> {
    log.debug("Fetching all templates by name $name in project $projectId")

    return templateDao.findAllByProject_ProjectIdAndName(projectId, name).map(MessageTemplateDbm::toDomain)
  }

  override fun fetchTemplatesByProjectId(projectId: Long): List<MessageTemplate> {
    log.debug("Fetching all templates by project ID $projectId")

    return templateDao.findAllByProject_ProjectId(projectId).map(MessageTemplateDbm::toDomain)
  }

  override fun fetchTemplatesByNameAndLanguage(
    projectId: Long,
    name: String,
    languageTag: String,
  ): List<MessageTemplate> {
    log.debug("Fetching all templates by name $name and language $languageTag in project $projectId")

    return templateDao.findAllByProject_ProjectIdAndNameAndLanguageTag(projectId, name, languageTag)
      .map(MessageTemplateDbm::toDomain)
  }

  override fun updateTemplate(updater: MessageTemplateUpdater): MessageTemplate {
    log.debug("Updating template using $updater")

    val template = templateDao.findById(updater.id).get().toDomain()
    val updated = updater.applyTo(template, timeProvider)

    return templateDao.save(updated.toData()).toDomain()
  }

  override fun deleteTemplateById(id: Long) {
    log.debug("Deleting template $id")

    return templateDao.deleteById(id)
  }

  override fun deleteAllTemplatesByProjectId(projectId: Long) {
    log.debug("Deleting all templates by project ID $projectId")

    return templateDao.deleteAllByProject_ProjectId(projectId)
  }

  override fun deleteAllTemplatesByName(projectId: Long, name: String) {
    log.debug("Deleting all templates by name $name in project $projectId")
    return templateDao.deleteAllByProject_ProjectIdAndName(projectId, name)
  }

}
