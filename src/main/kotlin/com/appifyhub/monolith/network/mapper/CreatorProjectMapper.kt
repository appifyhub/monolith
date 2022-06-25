package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.integrations.MailgunConfig
import com.appifyhub.monolith.domain.creator.setup.ProjectState
import com.appifyhub.monolith.network.integrations.MailgunConfigDto
import com.appifyhub.monolith.network.creator.project.ProjectFeatureResponse
import com.appifyhub.monolith.network.creator.project.ProjectResponse
import com.appifyhub.monolith.network.creator.project.ProjectStateResponse
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.service.access.AccessManager.Feature

fun Feature.toNetwork(): ProjectFeatureResponse = ProjectFeatureResponse(
  name = name,
  isRequired = isRequired,
)

fun ProjectState.toNetwork(): ProjectStateResponse = ProjectStateResponse(
  status = project.status.name,
  usableFeatures = usableFeatures.map(Feature::toNetwork),
  unusableFeatures = unusableFeatures.map(Feature::toNetwork),
)

fun MailgunConfig.toNetwork(): MailgunConfigDto = MailgunConfigDto(
  apiKey = apiKey,
  domain = domain,
  senderName = senderName,
  senderEmail = senderEmail,
)

fun Project.toNetwork(
  projectState: ProjectState,
): ProjectResponse = ProjectResponse(
  projectId = id,
  type = type.name,
  state = projectState.toNetwork(),
  userIdType = userIdType.name,
  name = name,
  description = description,
  logoUrl = logoUrl,
  websiteUrl = websiteUrl,
  maxUsers = maxUsers,
  anyoneCanSearch = anyoneCanSearch,
  onHold = onHold,
  languageTag = languageTag,
  mailgunConfig = mailgunConfig?.toNetwork(),
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  updatedAt = DateTimeMapper.formatAsDateTime(updatedAt),
)
