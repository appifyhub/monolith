package com.appifyhub.monolith.features.creator.api

import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.creator.api.model.messaging.MessageInputsRequest
import com.appifyhub.monolith.features.creator.api.model.messaging.MessageResponse
import com.appifyhub.monolith.features.creator.api.model.messaging.MessageTemplateCreateRequest
import com.appifyhub.monolith.features.creator.api.model.messaging.MessageTemplateResponse
import com.appifyhub.monolith.features.creator.api.model.messaging.MessageTemplateUpdateRequest
import com.appifyhub.monolith.features.creator.api.model.messaging.VariableResponse
import com.appifyhub.monolith.features.creator.domain.model.messaging.Message
import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplate
import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplateCreator
import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplateUpdater
import com.appifyhub.monolith.features.creator.domain.model.messaging.Variable
import com.appifyhub.monolith.features.creator.domain.service.MessageTemplateService.Inputs
import com.appifyhub.monolith.network.mapper.toDomainNonNull
import com.appifyhub.monolith.network.user.DateTimeMapper

fun Variable.toNetwork() = VariableResponse(
  code = code,
  example = example,
)

fun MessageTemplate.toNetwork() = MessageTemplateResponse(
  id = id,
  name = name,
  languageTag = languageTag,
  title = title,
  content = content,
  isHtml = isHtml,
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  updatedAt = DateTimeMapper.formatAsDateTime(updatedAt),
)

fun Message.toNetwork() = MessageResponse(
  template = template.toNetwork(),
  materialized = materialized,
)

fun MessageTemplateCreateRequest.toDomain(
  projectId: Long,
) = MessageTemplateCreator(
  projectId = projectId,
  name = name,
  languageTag = languageTag,
  title = title,
  content = content,
  isHtml = isHtml,
)

fun MessageTemplateUpdateRequest.toDomain(
  templateId: Long,
) = MessageTemplateUpdater(
  id = templateId,
  name = name.toDomainNonNull(),
  languageTag = languageTag.toDomainNonNull(),
  title = title.toDomainNonNull(),
  content = content.toDomainNonNull(),
  isHtml = isHtml.toDomainNonNull(),
)

fun MessageInputsRequest.toDomain() = Inputs(
  userId = universalUserId?.let { UserId.fromUniversalFormat(it) },
  projectId = projectId,
)
