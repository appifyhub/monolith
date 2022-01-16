package com.appifyhub.monolith.repository.messaging

import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.MessageTemplateCreator

interface MessageTemplateRepository {

  @Throws fun addTemplate(templateCreator: MessageTemplateCreator): MessageTemplate

  @Throws fun updateTemplate(template: MessageTemplate): MessageTemplate

  @Throws fun fetchTemplateById(id: Long): MessageTemplate

  @Throws fun deleteTemplateById(id: Long)

  @Throws fun fetchAllTemplatesByProjectId(projectId: Long): List<MessageTemplate>

  @Throws fun deleteAllTemplatesByProjectId(projectId: Long)

}
