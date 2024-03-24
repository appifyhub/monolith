package com.appifyhub.monolith.service.messaging

import com.appifyhub.monolith.domain.creator.messaging.Message
import com.appifyhub.monolith.domain.creator.messaging.MessageTemplate
import com.appifyhub.monolith.domain.creator.messaging.Variable
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateCreator
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId

interface MessageTemplateService {

  data class Inputs(
    val userId: UserId? = null,
    val overrideUser: User? = null, // used when raw password is passed around
    val projectId: Long? = null,
  )

  @Throws fun initializeDefaults()

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
