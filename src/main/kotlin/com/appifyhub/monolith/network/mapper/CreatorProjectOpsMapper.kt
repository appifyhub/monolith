package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.integrations.MailgunConfig
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.network.creator.integrations.MailgunConfigDto
import com.appifyhub.monolith.network.creator.project.ops.ProjectCreateRequest
import com.appifyhub.monolith.network.creator.project.ops.ProjectUpdateRequest
import com.appifyhub.monolith.service.creator.CreatorService.Companion.DEFAULT_MAX_USERS

fun MailgunConfigDto.toDomain(): MailgunConfig = MailgunConfig(
  apiKey = apiKey,
  domain = domain,
  senderName = senderName,
  senderEmail = senderEmail,
)

fun ProjectCreateRequest.toDomain(
  owner: User? = null,
): ProjectCreator = ProjectCreator(
  owner = owner,
  type = Project.Type.find(type, Project.Type.COMMERCIAL),
  status = Project.Status.REVIEW,
  userIdType = Project.UserIdType.find(userIdType, Project.UserIdType.RANDOM),
  name = name,
  description = description,
  logoUrl = logoUrl,
  websiteUrl = websiteUrl,
  maxUsers = DEFAULT_MAX_USERS,
  anyoneCanSearch = false,
  onHold = true,
  languageTag = languageTag,
  mailgunConfig = mailgunConfig?.toDomain(),
)

fun ProjectUpdateRequest.toDomain(
  projectId: Long,
): ProjectUpdater = ProjectUpdater(
  id = projectId,
  type = type.mapToDomainNonNull { Project.Type.find(it) },
  status = status.mapToDomainNonNull { Project.Status.find(it) },
  name = name.toDomainNonNull(),
  description = description.toDomainNullable(),
  logoUrl = logoUrl.toDomainNullable(),
  websiteUrl = websiteUrl.toDomainNullable(),
  maxUsers = maxUsers.toDomainNonNull(),
  anyoneCanSearch = anyoneCanSearch.toDomainNonNull(),
  onHold = onHold.toDomainNonNull(),
  languageTag = languageTag.toDomainNullable(),
  mailgunConfig = mailgunConfig.mapToDomainNullable { it.toDomain() },
)
