package com.appifyhub.monolith.features.creator.api

import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature
import com.appifyhub.monolith.features.creator.api.model.ProjectFeatureResponse
import com.appifyhub.monolith.features.creator.api.model.ProjectResponse
import com.appifyhub.monolith.features.creator.api.model.ProjectStateResponse
import com.appifyhub.monolith.features.creator.api.model.messaging.FirebaseConfigDto
import com.appifyhub.monolith.features.creator.api.model.messaging.MailgunConfigDto
import com.appifyhub.monolith.features.creator.api.model.messaging.TwilioConfigDto
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectState
import com.appifyhub.monolith.features.creator.domain.model.messaging.FirebaseConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.MailgunConfig
import com.appifyhub.monolith.features.creator.domain.model.messaging.TwilioConfig
import com.appifyhub.monolith.network.user.DateTimeMapper

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

fun TwilioConfig.toNetwork(): TwilioConfigDto = TwilioConfigDto(
  accountSid = accountSid,
  authToken = authToken,
  messagingServiceId = messagingServiceId,
  maxPricePerMessage = maxPricePerMessage,
  maxRetryAttempts = maxRetryAttempts,
  defaultSenderName = defaultSenderName,
  defaultSenderNumber = defaultSenderNumber,
)

fun FirebaseConfig.toNetwork(): FirebaseConfigDto = FirebaseConfigDto(
  projectName = projectName,
  serviceAccountKeyJsonBase64 = serviceAccountKeyJsonBase64,
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
  requiresSignupCodes = requiresSignupCodes,
  maxSignupCodesPerUser = maxSignupCodesPerUser,
  mailgunConfig = mailgunConfig?.toNetwork(),
  twilioConfig = twilioConfig?.toNetwork(),
  firebaseConfig = firebaseConfig?.toNetwork(),
  createdAt = DateTimeMapper.formatAsDateTime(createdAt),
  updatedAt = DateTimeMapper.formatAsDateTime(updatedAt),
)
