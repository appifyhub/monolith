package com.appifyhub.monolith.features.creator.domain

import com.appifyhub.monolith.domain.common.applySettable
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectCreator
import com.appifyhub.monolith.features.creator.domain.model.ProjectUpdater
import com.appifyhub.monolith.features.creator.domain.model.messaging.FirebaseConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.MailgunConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.TwilioConfig
import com.appifyhub.monolith.features.creator.storage.model.ProjectDbm
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
  .applySettable(requiresSignupCodes) { copy(requiresSignupCodes = it) }
  .applySettable(maxSignupCodesPerUser) { copy(maxSignupCodesPerUser = it) }
  .applySettable(mailgunConfig) { copy(mailgunConfig = it) }
  .applySettable(twilioConfig) { copy(twilioConfig = it) }
  .applySettable(firebaseConfig) { copy(firebaseConfig = it) }
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
  requiresSignupCodes = requiresSignupCodes,
  maxSignupCodesPerUser = maxSignupCodesPerUser,
  mailgunApiKey = mailgunConfig?.apiKey,
  mailgunDomain = mailgunConfig?.domain,
  mailgunSenderName = mailgunConfig?.senderName,
  mailgunSenderEmail = mailgunConfig?.senderEmail,
  twilioAccountSid = twilioConfig?.accountSid,
  twilioAuthToken = twilioConfig?.authToken,
  twilioMessagingServiceId = twilioConfig?.messagingServiceId,
  twilioMaxPricePerMessage = twilioConfig?.maxPricePerMessage,
  twilioMaxRetryAttempts = twilioConfig?.maxRetryAttempts,
  twilioDefaultSenderName = twilioConfig?.defaultSenderName,
  twilioDefaultSenderNumber = twilioConfig?.defaultSenderNumber,
  firebaseProjectName = firebaseConfig?.projectName,
  firebaseServiceAccountKeyJsonBase64 = firebaseConfig?.serviceAccountKeyJsonBase64,
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
  requiresSignupCodes = requiresSignupCodes,
  maxSignupCodesPerUser = maxSignupCodesPerUser,
  mailgunConfig = MailgunConfig(
    apiKey = mailgunApiKey.orEmpty(),
    domain = mailgunDomain.orEmpty(),
    senderName = mailgunSenderName.orEmpty(),
    senderEmail = mailgunSenderEmail.orEmpty(),
  ).takeIf { setOf(it.apiKey, it.domain, it.senderName, it.senderEmail).none(String::isEmpty) },
  twilioConfig = TwilioConfig(
    accountSid = twilioAccountSid.orEmpty(),
    authToken = twilioAuthToken.orEmpty(),
    messagingServiceId = twilioMessagingServiceId.orEmpty(),
    maxPricePerMessage = twilioMaxPricePerMessage ?: 0,
    maxRetryAttempts = twilioMaxRetryAttempts ?: 0,
    defaultSenderName = twilioDefaultSenderName.orEmpty(),
    defaultSenderNumber = twilioDefaultSenderNumber.orEmpty(),
  ).takeIf { setOf(it.accountSid, it.authToken, it.messagingServiceId, it.defaultSenderNumber).none(String::isEmpty) },
  firebaseConfig = FirebaseConfig(
    projectName = firebaseProjectName.orEmpty(),
    serviceAccountKeyJsonBase64 = firebaseServiceAccountKeyJsonBase64.orEmpty(),
  ).takeIf { setOf(it.projectName, it.serviceAccountKeyJsonBase64).none(String::isEmpty) },
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
  requiresSignupCodes = requiresSignupCodes,
  maxSignupCodesPerUser = maxSignupCodesPerUser,
  mailgunApiKey = mailgunConfig?.apiKey,
  mailgunDomain = mailgunConfig?.domain,
  mailgunSenderName = mailgunConfig?.senderName,
  mailgunSenderEmail = mailgunConfig?.senderEmail,
  twilioAccountSid = twilioConfig?.accountSid,
  twilioAuthToken = twilioConfig?.authToken,
  twilioMessagingServiceId = twilioConfig?.messagingServiceId,
  twilioMaxPricePerMessage = twilioConfig?.maxPricePerMessage,
  twilioMaxRetryAttempts = twilioConfig?.maxRetryAttempts,
  twilioDefaultSenderName = twilioConfig?.defaultSenderName,
  twilioDefaultSenderNumber = twilioConfig?.defaultSenderNumber,
  firebaseProjectName = firebaseConfig?.projectName,
  firebaseServiceAccountKeyJsonBase64 = firebaseConfig?.serviceAccountKeyJsonBase64,
  createdAt = createdAt,
  updatedAt = updatedAt,
)
