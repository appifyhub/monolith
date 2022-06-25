package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.common.applySettable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.integrations.MailgunConfig
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.storage.model.creator.ProjectDbm
import com.appifyhub.monolith.util.TimeProvider

fun ProjectUpdater.applyTo(
  project: Project,
  timeProvider: TimeProvider,
): Project = project
  .applySettable(type) { copy(type = it) }
  .applySettable(status) { copy(status = it) }
  .applySettable(name) { copy(name = it) }
  .applySettable(description) { copy(description = it) }
  .applySettable(logoUrl) { copy(logoUrl = it) }
  .applySettable(websiteUrl) { copy(websiteUrl = it) }
  .applySettable(maxUsers) { copy(maxUsers = it) }
  .applySettable(anyoneCanSearch) { copy(anyoneCanSearch = it) }
  .applySettable(onHold) { copy(onHold = it) }
  .applySettable(languageTag) { copy(languageTag = it) }
  .applySettable(mailgunConfig) { copy(mailgunConfig = it) }
  .copy(updatedAt = timeProvider.currentDate)

fun ProjectCreator.toProjectData(
  timeProvider: TimeProvider,
): ProjectDbm = ProjectDbm(
  projectId = null,
  type = type.name,
  status = status.name,
  userIdType = userIdType.name,
  name = name,
  description = description,
  logoUrl = logoUrl,
  websiteUrl = websiteUrl,
  maxUsers = maxUsers,
  anyoneCanSearch = anyoneCanSearch,
  onHold = onHold,
  languageTag = languageTag,
  mailgunApiKey = mailgunConfig?.apiKey,
  mailgunDomain = mailgunConfig?.domain,
  mailgunSenderName = mailgunConfig?.senderName,
  mailgunSenderEmail = mailgunConfig?.senderEmail,
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
)

fun ProjectDbm.toDomain(): Project = Project(
  id = projectId!!,
  type = Project.Type.find(type, default = Project.Type.COMMERCIAL),
  status = Project.Status.find(status, default = Project.Status.REVIEW),
  userIdType = Project.UserIdType.find(userIdType, default = Project.UserIdType.RANDOM),
  name = name,
  description = description,
  logoUrl = logoUrl,
  websiteUrl = websiteUrl,
  maxUsers = maxUsers,
  anyoneCanSearch = anyoneCanSearch,
  onHold = onHold,
  languageTag = languageTag,
  mailgunConfig = MailgunConfig(
    apiKey = mailgunApiKey.orEmpty(),
    domain = mailgunDomain.orEmpty(),
    senderName = mailgunSenderName.orEmpty(),
    senderEmail = mailgunSenderEmail.orEmpty(),
  ),
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Project.toData(): ProjectDbm = ProjectDbm(
  projectId = id,
  type = type.name,
  status = status.name,
  userIdType = userIdType.name,
  name = name,
  description = description,
  logoUrl = logoUrl,
  websiteUrl = websiteUrl,
  maxUsers = maxUsers,
  anyoneCanSearch = anyoneCanSearch,
  onHold = onHold,
  languageTag = languageTag,
  mailgunApiKey = mailgunConfig?.apiKey,
  mailgunDomain = mailgunConfig?.domain,
  mailgunSenderName = mailgunConfig?.senderName,
  mailgunSenderEmail = mailgunConfig?.senderEmail,
  createdAt = createdAt,
  updatedAt = updatedAt,
)
