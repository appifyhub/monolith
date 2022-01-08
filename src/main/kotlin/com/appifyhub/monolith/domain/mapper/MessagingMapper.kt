package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.common.stubMessageTemplate
import com.appifyhub.monolith.domain.common.stubProject
import com.appifyhub.monolith.domain.messaging.TemplateDataBinder
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.VariableBinding
import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import com.appifyhub.monolith.storage.model.messaging.VariableBindingDbm
import com.appifyhub.monolith.storage.model.messaging.VariableBindingKeyDbm

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

fun MessageTemplate.toData(
  projectId: Long,
): MessageTemplateDbm = MessageTemplateDbm(
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

fun VariableBindingDbm.toDomain(): VariableBinding = VariableBinding(
  variableName = id.variableName,
  bindsTo = TemplateDataBinder.Code.find(bindingCode),
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
  ).toData(projectId),
  bindingCode = bindsTo.code,
  createdAt = createdAt,
  updatedAt = updatedAt,

)
