package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.common.stubMessageTemplate
import com.appifyhub.monolith.domain.common.stubProject
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.MessageTemplateCreator
import com.appifyhub.monolith.domain.messaging.binding.TemplateDataBinder.Code
import com.appifyhub.monolith.domain.messaging.VariableBinding
import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import com.appifyhub.monolith.storage.model.messaging.VariableBindingDbm
import com.appifyhub.monolith.storage.model.messaging.VariableBindingKeyDbm
import com.appifyhub.monolith.util.TimeProvider

fun MessageTemplateDbm.toDomain(): MessageTemplate = MessageTemplate(
  id = id!!,
  projectId = project.projectId!!,
  name = name,
  language = language,
  content = content,
  isHtml = isHtml,
  bindings = bindings.map(VariableBindingDbm::toDomain),
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun MessageTemplate.toData(): MessageTemplateDbm = MessageTemplateDbm(
  id = id,
  project = stubProject().copy(id = projectId).toData(),
  name = name,
  language = language,
  content = content,
  isHtml = isHtml,
  bindings = bindings.map { it.toData(id, projectId) },
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun MessageTemplateCreator.toData(
  timeProvider: TimeProvider,
): MessageTemplateDbm = MessageTemplateDbm(
  id = null,
  project = stubProject().copy(id = projectId).toData(),
  name = name,
  language = language,
  content = content,
  isHtml = isHtml,
  bindings = emptyList(),
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
)

fun VariableBindingDbm.toDomain(): VariableBinding = VariableBinding(
  variableName = id.variableName,
  bindsTo = Code.findByCode(bindingCode),
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun VariableBinding.toData(
  templateId: Long,
  projectId: Long,
): VariableBindingDbm = VariableBindingDbm(
  id = VariableBindingKeyDbm(
    templateId = templateId,
    variableName = variableName,
  ),
  template = stubMessageTemplate().copy(
    id = templateId,
    projectId = projectId,
  ).toData(),
  bindingCode = bindsTo.code,
  createdAt = createdAt,
  updatedAt = updatedAt,

)
