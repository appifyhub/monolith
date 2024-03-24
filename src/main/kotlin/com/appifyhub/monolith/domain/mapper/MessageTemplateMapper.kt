package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.common.applySettable
import com.appifyhub.monolith.domain.common.stubProject
import com.appifyhub.monolith.domain.creator.messaging.MessageTemplate
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateCreator
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateUpdater
import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import com.appifyhub.monolith.util.TimeProvider

fun MessageTemplateDbm.toDomain(): MessageTemplate = MessageTemplate(
  id = id!!,
  projectId = project.projectId!!,
  name = name,
  languageTag = languageTag,
  title = title,
  content = content,
  isHtml = isHtml,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun MessageTemplate.toData(): MessageTemplateDbm = MessageTemplateDbm(
  id = id,
  project = stubProject().copy(id = projectId).toData(),
  name = name,
  languageTag = languageTag,
  title = title,
  content = content,
  isHtml = isHtml,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun MessageTemplateCreator.toData(
  timeProvider: TimeProvider,
): MessageTemplateDbm = MessageTemplateDbm(
  id = null,
  project = stubProject().copy(id = projectId).toData(),
  name = name,
  languageTag = languageTag,
  title = title,
  content = content,
  isHtml = isHtml,
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
)

fun MessageTemplateUpdater.applyTo(
  template: MessageTemplate,
  timeProvider: TimeProvider,
): MessageTemplate = template
  .applySettable(name) { copy(name = it) }
  .applySettable(languageTag) { copy(languageTag = it) }
  .applySettable(title) { copy(title = it) }
  .applySettable(content) { copy(content = it) }
  .applySettable(isHtml) { copy(isHtml = it) }
  .copy(updatedAt = timeProvider.currentDate)
