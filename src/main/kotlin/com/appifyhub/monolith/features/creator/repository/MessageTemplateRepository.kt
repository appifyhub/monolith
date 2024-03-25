package com.appifyhub.monolith.features.creator.repository

import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplate
import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplateCreator
import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplateUpdater

interface MessageTemplateRepository {

  @Throws fun addTemplate(templateCreator: MessageTemplateCreator): MessageTemplate

  @Throws fun fetchTemplateById(id: Long): MessageTemplate

  @Throws fun fetchTemplatesByName(projectId: Long, name: String): List<MessageTemplate>

  @Throws fun fetchTemplatesByNameAndLanguage(projectId: Long, name: String, languageTag: String): List<MessageTemplate>

  @Throws fun fetchTemplatesByProjectId(projectId: Long): List<MessageTemplate>

  @Throws fun updateTemplate(updater: MessageTemplateUpdater): MessageTemplate

  @Throws fun deleteTemplateById(id: Long)

  @Throws fun deleteAllTemplatesByProjectId(projectId: Long)

  @Throws fun deleteAllTemplatesByName(projectId: Long, name: String)

}
