package com.appifyhub.monolith.features.creator.api

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.features.creator.api.model.ProjectCreateRequest
import com.appifyhub.monolith.features.creator.api.model.ProjectUpdateRequest
import com.appifyhub.monolith.features.creator.api.model.messaging.FirebaseConfigDto
import com.appifyhub.monolith.features.creator.api.model.messaging.MailgunConfigDto
import com.appifyhub.monolith.features.creator.api.model.messaging.TwilioConfigDto
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectCreator
import com.appifyhub.monolith.features.creator.domain.model.ProjectUpdater
import com.appifyhub.monolith.features.creator.domain.model.messaging.FirebaseConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.MailgunConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.TwilioConfig
import com.appifyhub.monolith.features.creator.domain.service.CreatorService.Companion.DEFAULT_MAX_USERS
import com.appifyhub.monolith.network.mapper.mapToDomainNonNull
import com.appifyhub.monolith.network.mapper.mapToDomainNullable
import com.appifyhub.monolith.network.mapper.toDomainNonNull
import com.appifyhub.monolith.network.mapper.toDomainNullable

fun MailgunConfigDto.toDomain(): MailgunConfig = MailgunConfig(
  apiKey = apiKey,
  domain = domain,
  senderName = senderName,
  senderEmail = senderEmail,
)

fun TwilioConfigDto.toDomain(): TwilioConfig = TwilioConfig(
  accountSid = accountSid,
  authToken = authToken,
  messagingServiceId = messagingServiceId,
  maxPricePerMessage = maxPricePerMessage,
  maxRetryAttempts = maxRetryAttempts,
  defaultSenderName = defaultSenderName,
  defaultSenderNumber = defaultSenderNumber,
)

fun FirebaseConfigDto.toDomain(): FirebaseConfig = FirebaseConfig(
  projectName = projectName,
  serviceAccountKeyJsonBase64 = serviceAccountKeyJsonBase64,
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
  maxUsers = maxUsers ?: DEFAULT_MAX_USERS,
  anyoneCanSearch = anyoneCanSearch ?: false,
  onHold = true,
  languageTag = languageTag,
  requiresSignupCodes = requiresSignupCodes ?: false,
  maxSignupCodesPerUser = maxSignupCodesPerUser ?: Integer.MAX_VALUE,
  mailgunConfig = mailgunConfig?.toDomain(),
  twilioConfig = twilioConfig?.toDomain(),
  firebaseConfig = firebaseConfig?.toDomain(),
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
  requiresSignupCodes = requiresSignupCodes.toDomainNonNull(),
  maxSignupCodesPerUser = maxSignupCodesPerUser.toDomainNonNull(),
  mailgunConfig = mailgunConfig.mapToDomainNullable { it.toDomain() },
  twilioConfig = twilioConfig.mapToDomainNullable { it.toDomain() },
  firebaseConfig = firebaseConfig.mapToDomainNullable { it.toDomain() },
)
