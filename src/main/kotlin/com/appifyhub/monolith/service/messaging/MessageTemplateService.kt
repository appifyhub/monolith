package com.appifyhub.monolith.service.messaging

import com.appifyhub.monolith.domain.messaging.Message
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.MessageTemplateCreator
import com.appifyhub.monolith.domain.messaging.Variable
import com.appifyhub.monolith.domain.messaging.ops.MessageTemplateUpdater
import com.appifyhub.monolith.domain.user.UserId

interface MessageTemplateService {

  data class Inputs(
    val userId: UserId? = null,
    val projectId: Long? = null,
  )

  @Throws fun addTemplate(creator: MessageTemplateCreator): MessageTemplate

  @Throws fun fetchTemplateById(id: Long): MessageTemplate

  @Throws fun fetchTemplatesByName(projectId: Long, name: String): List<MessageTemplate>

  @Throws fun fetchTemplatesByProjectId(projectId: Long): List<MessageTemplate>

  @Throws fun fetchTemplatesByNameAndLanguage(projectId: Long, name: String, languageTag: String): List<MessageTemplate>

  @Throws fun updateTemplate(updater: MessageTemplateUpdater): MessageTemplate

  @Throws fun deleteTemplateById(id: Long)

  @Throws fun deleteAllTemplatesByProjectId(projectId: Long)

  @Throws fun deleteAllTemplatesByName(projectId: Long, name: String)

  @Throws fun detectVariables(content: String): Set<Variable>

  @Throws fun materializeById(templateId: Long, inputs: Inputs): Message

  @Throws fun materializeByName(projectId: Long, templateName: String, inputs: Inputs): Message

}
