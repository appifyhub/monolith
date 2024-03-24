package com.appifyhub.monolith.repository.messaging

import com.appifyhub.monolith.domain.creator.messaging.MessageTemplate
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateCreator
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateUpdater

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
