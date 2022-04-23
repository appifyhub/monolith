package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.messaging.Message
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.Variable
import com.appifyhub.monolith.domain.messaging.ops.MessageTemplateCreator
import com.appifyhub.monolith.domain.messaging.ops.MessageTemplateUpdater
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.messaging.MessageResponse
import com.appifyhub.monolith.network.messaging.MessageTemplateResponse
import com.appifyhub.monolith.network.messaging.VariableResponse
import com.appifyhub.monolith.network.messaging.ops.MessageInputsRequest
import com.appifyhub.monolith.network.messaging.ops.MessageTemplateCreateRequest
import com.appifyhub.monolith.network.messaging.ops.MessageTemplateUpdateRequest
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
  content = content,
  isHtml = isHtml,
)

fun MessageTemplateUpdateRequest.toDomain(
  templateId: Long,
) = MessageTemplateUpdater(
  id = templateId,
  name = name.toDomainNonNull(),
  languageTag = languageTag.toDomainNonNull(),
  content = content.toDomainNonNull(),
  isHtml = isHtml.toDomainNonNull(),
)

fun MessageInputsRequest.toDomain() = Inputs(
  userId = universalUserId?.let { UserId.fromUniversalFormat(it) },
  projectId = projectId,
)
