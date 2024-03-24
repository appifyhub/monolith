package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.creator.messaging.Message
import com.appifyhub.monolith.domain.creator.messaging.MessageTemplate
import com.appifyhub.monolith.domain.creator.messaging.Variable
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateCreator
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateUpdater
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.creator.messaging.MessageResponse
import com.appifyhub.monolith.network.creator.messaging.MessageTemplateResponse
import com.appifyhub.monolith.network.creator.messaging.VariableResponse
import com.appifyhub.monolith.network.creator.messaging.ops.MessageInputsRequest
import com.appifyhub.monolith.network.creator.messaging.ops.MessageTemplateCreateRequest
import com.appifyhub.monolith.network.creator.messaging.ops.MessageTemplateUpdateRequest
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.service.messaging.MessageTemplateService.Inputs

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
