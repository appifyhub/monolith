package com.appifyhub.monolith.util

import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.features.auth.domain.model.TokenCreator
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.creator.setup.ProjectState
import com.appifyhub.monolith.domain.geo.Geolocation
import com.appifyhub.monolith.domain.integrations.FirebaseConfig
import com.appifyhub.monolith.domain.integrations.MailgunConfig
import com.appifyhub.monolith.domain.integrations.TwilioConfig
import com.appifyhub.monolith.domain.creator.messaging.Message
import com.appifyhub.monolith.domain.creator.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.PushDevice
import com.appifyhub.monolith.domain.creator.messaging.Variable
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateCreator
import com.appifyhub.monolith.domain.creator.messaging.ops.MessageTemplateUpdater
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.network.creator.user.ops.CreatorCredentialsRequest
import com.appifyhub.monolith.features.auth.api.model.TokenDetailsResponse
import com.appifyhub.monolith.features.auth.api.model.TokenResponse
import com.appifyhub.monolith.features.auth.api.model.UserCredentialsRequest
import com.appifyhub.monolith.network.common.SettableRequest
import com.appifyhub.monolith.network.creator.project.ProjectFeatureResponse
import com.appifyhub.monolith.network.creator.project.ProjectResponse
import com.appifyhub.monolith.network.creator.project.ProjectStateResponse
import com.appifyhub.monolith.network.creator.project.ops.ProjectCreateRequest
import com.appifyhub.monolith.network.creator.project.ops.ProjectUpdateRequest
import com.appifyhub.monolith.network.creator.user.ops.CreatorSignupRequest
import com.appifyhub.monolith.network.integrations.FirebaseConfigDto
import com.appifyhub.monolith.network.integrations.MailgunConfigDto
import com.appifyhub.monolith.network.integrations.TwilioConfigDto
import com.appifyhub.monolith.network.creator.messaging.MessageResponse
import com.appifyhub.monolith.network.creator.messaging.MessageTemplateResponse
import com.appifyhub.monolith.network.messaging.PushDeviceResponse
import com.appifyhub.monolith.network.messaging.PushDevicesResponse
import com.appifyhub.monolith.network.creator.messaging.VariableResponse
import com.appifyhub.monolith.network.creator.messaging.ops.MessageInputsRequest
import com.appifyhub.monolith.network.messaging.ops.MessageSendRequest
import com.appifyhub.monolith.network.creator.messaging.ops.MessageTemplateCreateRequest
import com.appifyhub.monolith.network.creator.messaging.ops.MessageTemplateUpdateRequest
import com.appifyhub.monolith.network.messaging.ops.PushDeviceRequest
import com.appifyhub.monolith.network.user.OrganizationDto
import com.appifyhub.monolith.network.user.SignupCodeResponse
import com.appifyhub.monolith.network.user.SignupCodesResponse
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.network.user.ops.OrganizationUpdaterDto
import com.appifyhub.monolith.network.user.ops.UserSignupRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateAuthorityRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateDataRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateSignatureRequest
import com.appifyhub.monolith.features.auth.domain.security.JwtClaims
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature
import com.appifyhub.monolith.service.integrations.CommunicationsService
import com.appifyhub.monolith.service.messaging.MessageTemplateService.Inputs
import com.appifyhub.monolith.features.auth.storage.model.TokenDetailsDbm
import com.appifyhub.monolith.storage.model.creator.ProjectCreationDbm
import com.appifyhub.monolith.storage.model.creator.ProjectCreationKeyDbm
import com.appifyhub.monolith.storage.model.creator.ProjectDbm
import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import com.appifyhub.monolith.storage.model.messaging.PushDeviceDbm
import com.appifyhub.monolith.storage.model.schema.SchemaDbm
import com.appifyhub.monolith.storage.model.user.OrganizationDbm
import com.appifyhub.monolith.storage.model.user.SignupCodeDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("MayBeConstant")
object Stubs {

  // region Tokens

  // signed with debug key, will expire in 2026
  @Suppress("SpellCheckingInspection")
  val tokenValue = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9." +
    "eyJnZW8iOiJnZW8iLCJzdWIiOiJ1c2VybmFtZSQyIiwidXNlcl9pZCI6InVzZXJu" +
    "YW1lIiwicHJvamVjdF9pZCI6IjIiLCJpc19zdGF0aWMiOiJmYWxzZSIsIm9yaWdp" +
    "biI6IlRva2VuIE9yaWdpbiIsImlwX2FkZHJlc3MiOiIxLjIuMy40IiwiZXhwIjox" +
    "NzkyNjk3NzU4LCJ1bml2ZXJzYWxfaWQiOiJ1c2VybmFtZSQyIiwiaWF0IjoxNjE5" +
    "ODk3NzU4LCJhdXRob3JpdGllcyI6IkRFRkFVTFQsTU9ERVJBVE9SLEFETUlOIn0." +
    "Oe1CIh1vcTrMlM0bPC1Nv_9xcA0rArxp-QO6n_67jEoNEKfPDM1cFbFyLxtUCuTd" +
    "RUbxVji6020RqUn3SR033SweRXfMQnvm83hdDrstWs6EaK5t6AHNW2VALMqpTiBu" +
    "k585_tCP6WWkj7khf_19Ly8YlTp3XFNadANUgB9-mYq1n_9COc2OzHuub7o4-OLJ" +
    "tPE3NpPjB_v0kPRI5-Lz7dVaWTzkCjTPAdrwhe6NtZhg9IMtpZxVu-5aVh8iC9lD" +
    "uKgTCrgQNcLnLX0hFpP58-s_kS1SpPsAl6266UUQJpXEJQPoZ8Q06aLI-W2vJH25" +
    "J3-IaTDFIMexa64_cA8I0Q"

  // signed with debug key, will expire in 2026
  @Suppress("SpellCheckingInspection")
  val tokenValueStatic = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdW" +
    "IiOiJ1c2VybmFtZSQyIiwiYWNjb3VudF9pZCI6IjEiLCJ1c2VyX2lkIjoidXNlcm" +
    "5hbWUiLCJwcm9qZWN0X2lkIjoiMiIsImlzX3N0YXRpYyI6InRydWUiLCJvcmlnaW" +
    "4iOiJUb2tlbiBPcmlnaW4iLCJleHAiOjE3Nzk0ODI0OTQsInVuaXZlcnNhbF9pZC" +
    "I6InVzZXJuYW1lJDIiLCJpYXQiOjE2MjE2OTc3MzQsImF1dGhvcml0aWVzIjoiRE" +
    "VGQVVMVCxNT0RFUkFUT1IsQURNSU4ifQ.hz8bqB2P2UdhionSyL5umquuXjZ2KKC" +
    "81AADgzwaqkpp3rPZnyiL13VQdKbMmmC4mcg1q7_mikLERfvdN9f3E-IBMQmU_0O" +
    "S9dzN9Il9Q1rshpzDqSCGndQZBfQT1TTFibZzAarB1zIUzSLhHsKMoOhxxNDourW" +
    "QdfEUlmLC5jbNKh4gOPEBCvFxvTJ2SPaeONsL-aOaI9naXSIGWTOptALVGs9oTL3" +
    "bKyAyQ-RnPt_fVpe042vBqQRlu11aZw6Nbtn4vMStwR_GCnIy9dK4AaQCjkIu1JY" +
    "93uFL6VEbzcZd1GuteWC3PRMD0fu6O9CsGxJUTnqcT4_ifUHy0cBItQ"

  // endregion

  // region Domain Models

  val universalUserId = "username$1"

  val userId = UserId(
    userId = "username",
    projectId = 1,
  )

  val company = Organization(
    name = "Company",
    street = "Street Name 1",
    postcode = "12345",
    city = "City",
    countryCode = "DE",
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val companyUpdated = company.copy(
    name = "Company 1",
    street = "Street Name 11",
    postcode = "123451",
    city = "City 1",
    countryCode = "DF",
  )

  val ipAddress = "173.85.251.191"

  val geo = Geolocation(
    countryCode = "US",
    countryName = "United States of America",
    region = "Minnesota",
    city = "Lakeville",
  )

  val geoMerged = "US, United States of America, Minnesota, Lakeville"

  var tokenDetails = TokenDetails(
    tokenValue = tokenValue,
    isBlocked = true,
    createdAt = Date(TimeUnit.SECONDS.toMillis(1619897758)), // decode token value
    expiresAt = Date(TimeUnit.SECONDS.toMillis(1792697758)), // decode token value
    ownerId = userId,
    authority = User.Authority.ADMIN,
    origin = "Token Origin",
    ipAddress = ipAddress,
    geo = geoMerged,
    isStatic = false,
  )

  @Suppress("MemberVisibilityCanBePrivate")
  var tokenDetailsStatic = TokenDetails(
    tokenValue = tokenValueStatic,
    isBlocked = true,
    createdAt = Date(TimeUnit.SECONDS.toMillis(1621697734)), // decode token value
    expiresAt = Date(TimeUnit.SECONDS.toMillis(1779482494)), // decode token value
    ownerId = userId,
    authority = User.Authority.ADMIN,
    origin = "Token Origin",
    ipAddress = ipAddress,
    geo = geoMerged,
    isStatic = true,
  )

  var user = User(
    id = userId,
    signature = "drowssap",
    name = "User's Name",
    type = User.Type.ORGANIZATION,
    authority = User.Authority.ADMIN,
    allowsSpam = true,
    contact = "user@example.com",
    contactType = User.ContactType.EMAIL,
    verificationToken = "abcd1234",
    birthday = Date(0xB00000),
    company = company,
    languageTag = Locale.US.toLanguageTag(),
    createdAt = Date(0xC00000),
    updatedAt = Date(0xA00000),
  )

  var userUpdated = user.copy(
    id = user.id,
    signature = "1drowssap",
    name = "User's Name 1",
    type = User.Type.PERSONAL,
    authority = User.Authority.MODERATOR,
    allowsSpam = false,
    contact = "+491760000001",
    contactType = User.ContactType.PHONE,
    verificationToken = "abcd12341",
    birthday = Date(0x10B00001),
    company = companyUpdated,
    languageTag = Locale.UK.toLanguageTag(),
    createdAt = user.createdAt,
    updatedAt = Date(0xA00001),
  )

  private val mailgunConfig = MailgunConfig(
    apiKey = "apiKey",
    domain = "domain.com",
    senderName = "senderName",
    senderEmail = "senderEmail@domain.com",
  )

  private val mailgunConfigUpdated = MailgunConfig(
    apiKey = "apiKey1",
    domain = "domain1.com",
    senderName = "senderName1",
    senderEmail = "senderEmail1@domain.com",
  )

  private val twilioConfig = TwilioConfig(
    accountSid = "accountSid",
    authToken = "authToken",
    messagingServiceId = "messagingServiceId",
    maxPricePerMessage = 2,
    maxRetryAttempts = 2,
    defaultSenderName = "defSenderName",
    defaultSenderNumber = "+491760000000",
  )

  private val twilioConfigUpdated = TwilioConfig(
    accountSid = "accountSid1",
    authToken = "authToken1",
    messagingServiceId = "messagingServiceId1",
    maxPricePerMessage = 3,
    maxRetryAttempts = 3,
    defaultSenderName = "defSenderName1",
    defaultSenderNumber = "+491760000001",
  )

  private val firebaseConfig = FirebaseConfig(
    projectName = "projectName",
    serviceAccountKeyJsonBase64 = "c2VydmljZUFjY291bnRLZXlKc29uQmFzZTY0", // base64("serviceAccountKeyJsonBase64")
  )

  private val firebaseConfigUpdated = FirebaseConfig(
    projectName = "projectName1",
    serviceAccountKeyJsonBase64 = "c2VydmljZUFjY291bnRLZXlKc29uQmFzZTY0MQ==", // base64("serviceAccountKeyJsonBase641")
  )

  val project = Project(
    id = userId.projectId,
    type = Project.Type.OPENSOURCE,
    status = Project.Status.ACTIVE,
    userIdType = Project.UserIdType.USERNAME,
    name = "name",
    description = "description",
    logoUrl = "logoUrl",
    websiteUrl = "websiteUrl",
    maxUsers = 1000,
    anyoneCanSearch = true,
    onHold = true,
    languageTag = Locale.US.toLanguageTag(),
    requiresSignupCodes = false,
    maxSignupCodesPerUser = Integer.MAX_VALUE,
    mailgunConfig = mailgunConfig,
    twilioConfig = twilioConfig,
    firebaseConfig = firebaseConfig,
    createdAt = Date(0xC20000),
    updatedAt = Date(0xA20000),
  )

  val projectUpdated = Project(
    id = project.id,
    type = Project.Type.FREE,
    status = Project.Status.SUSPENDED,
    userIdType = project.userIdType,
    name = "name1",
    description = "description1",
    logoUrl = "logoUrl1",
    websiteUrl = "websiteUrl1",
    maxUsers = 1001,
    anyoneCanSearch = false,
    onHold = false,
    languageTag = Locale.UK.toLanguageTag(),
    requiresSignupCodes = true,
    maxSignupCodesPerUser = 5,
    mailgunConfig = mailgunConfigUpdated,
    twilioConfig = twilioConfigUpdated,
    firebaseConfig = firebaseConfigUpdated,
    createdAt = project.createdAt,
    updatedAt = Date(0xA20001),
  )

  val schema = Schema(
    version = 1,
    isInitialized = true,
  )

  val projectState = ProjectState(
    project = project,
    usableFeatures = listOf(Feature.BASIC),
    unusableFeatures = emptyList(),
  )

  val messageTemplate = MessageTemplate(
    id = 10,
    projectId = project.id,
    name = "template",
    languageTag = Locale.US.toLanguageTag(),
    title = "title",
    content = "content",
    isHtml = true,
    createdAt = Date(0x10000E),
    updatedAt = Date(0x1F0000),
  )

  val messageTemplateUpdated = MessageTemplate(
    id = messageTemplate.id,
    projectId = project.id,
    name = "template1",
    languageTag = Locale.UK.toLanguageTag(),
    title = "title1",
    content = "content1",
    isHtml = false,
    createdAt = Date(0x10000E),
    updatedAt = Date(0x10001F),
  )

  val message = Message(
    template = messageTemplate,
    materialized = "content",
  )

  val pushDevice = PushDevice(
    deviceId = "push_token",
    type = PushDevice.Type.ANDROID,
    owner = user,
  )

  val signupCode = SignupCode(
    code = "code",
    isUsed = false,
    owner = user,
    createdAt = Date(0x10000E),
    usedAt = null,
  )

  // endregion

  // region Domain Ops Models

  val userCreator = UserCreator(
    userId = "username",
    projectId = project.id,
    rawSignature = "password",
    name = "User's Name",
    type = User.Type.ORGANIZATION,
    authority = User.Authority.ADMIN,
    allowsSpam = true,
    contact = "user@example.com",
    contactType = User.ContactType.EMAIL,
    birthday = Date(0xB00000),
    company = company,
    languageTag = Locale.US.toLanguageTag(),
    signupCode = "FAKE-CODE-1234",
  )

  val companyUpdater = OrganizationUpdater(
    name = Settable("Company 1"),
    street = Settable("Street Name 11"),
    postcode = Settable("123451"),
    city = Settable("City 1"),
    countryCode = Settable("DF"),
  )

  val userUpdater = UserUpdater(
    id = userId,
    rawSignature = Settable("password1"),
    type = Settable(User.Type.PERSONAL),
    authority = Settable(User.Authority.MODERATOR),
    contactType = Settable(User.ContactType.PHONE),
    allowsSpam = Settable(false),
    name = Settable("User's Name 1"),
    contact = Settable("+491760000001"),
    verificationToken = Settable("abcd12341"),
    birthday = Settable(Date(0x10B00001)),
    company = Settable(companyUpdater),
    languageTag = Settable(Locale.UK.toLanguageTag()),
  )

  val projectCreator = ProjectCreator(
    owner = null,
    type = Project.Type.OPENSOURCE,
    status = Project.Status.ACTIVE,
    userIdType = Project.UserIdType.USERNAME,
    name = "name",
    description = "description",
    logoUrl = "logoUrl",
    websiteUrl = "websiteUrl",
    maxUsers = 1000,
    anyoneCanSearch = true,
    onHold = true,
    languageTag = Locale.US.toLanguageTag(),
    requiresSignupCodes = false,
    maxSignupCodesPerUser = Integer.MAX_VALUE,
    mailgunConfig = mailgunConfig,
    twilioConfig = twilioConfig,
    firebaseConfig = firebaseConfig,
  )

  val projectUpdater = ProjectUpdater(
    id = project.id,
    type = Settable(Project.Type.FREE),
    status = Settable(Project.Status.SUSPENDED),
    name = Settable("name1"),
    description = Settable("description1"),
    logoUrl = Settable("logoUrl1"),
    websiteUrl = Settable("websiteUrl1"),
    maxUsers = Settable(1001),
    anyoneCanSearch = Settable(false),
    onHold = Settable(false),
    languageTag = Settable(Locale.UK.toLanguageTag()),
    requiresSignupCodes = Settable(true),
    maxSignupCodesPerUser = Settable(5),
    mailgunConfig = Settable(mailgunConfigUpdated),
    twilioConfig = Settable(twilioConfigUpdated),
    firebaseConfig = Settable(firebaseConfigUpdated),
  )

  val messageTemplateCreator = MessageTemplateCreator(
    projectId = project.id,
    name = messageTemplate.name,
    languageTag = messageTemplate.languageTag,
    title = messageTemplate.title,
    content = messageTemplate.content,
    isHtml = messageTemplate.isHtml,
  )

  val messageTemplateUpdater = MessageTemplateUpdater(
    id = messageTemplate.id,
    name = Settable("template1"),
    languageTag = Settable(Locale.UK.toLanguageTag()),
    title = Settable("title1"),
    content = Settable("content1"),
    isHtml = Settable(false),
  )

  val messageInputs = Inputs(
    userId = user.id,
    projectId = project.id,
  )

  // endregion

  // region Auth Ops Models

  val tokenCreator = TokenCreator(
    id = userId,
    authority = User.Authority.ADMIN,
    isStatic = false,
    origin = "Token Origin",
    ipAddress = ipAddress,
    geo = geoMerged,
  )

  val jwtClaims: JwtClaims = mapOf(
    Claims.VALUE to tokenValue,
    Claims.USER_ID to userId.userId,
    Claims.PROJECT_ID to project.id,
    Claims.UNIVERSAL_ID to userId.toUniversalFormat(),
    Claims.CREATED_AT to TimeUnit.MILLISECONDS.toSeconds(tokenDetails.createdAt.time).toInt(),
    Claims.EXPIRES_AT to TimeUnit.MILLISECONDS.toSeconds(tokenDetails.expiresAt.time).toInt(),
    Claims.AUTHORITIES to User.Authority.ADMIN.allAuthorities.joinToString(",") { it.authority },
    Claims.ORIGIN to "Token Origin",
    Claims.IP_ADDRESS to ipAddress,
    Claims.GEO to geoMerged,
    Claims.IS_STATIC to false,
  )

  val jwtClaimsStatic: JwtClaims = mapOf(
    Claims.VALUE to tokenValueStatic,
    Claims.USER_ID to userId.userId,
    Claims.PROJECT_ID to project.id,
    Claims.UNIVERSAL_ID to userId.toUniversalFormat(),
    Claims.CREATED_AT to TimeUnit.MILLISECONDS.toSeconds(tokenDetailsStatic.createdAt.time).toInt(),
    Claims.EXPIRES_AT to TimeUnit.MILLISECONDS.toSeconds(tokenDetailsStatic.expiresAt.time).toInt(),
    Claims.AUTHORITIES to User.Authority.ADMIN.allAuthorities.joinToString(",") { it.authority },
    Claims.ORIGIN to "Token Origin",
    Claims.IP_ADDRESS to ipAddress,
    Claims.GEO to geoMerged,
    Claims.IS_STATIC to true,
  )

  // endregion

  // region Database Models

  val userIdDbm = UserIdDbm(
    userId = "username",
    projectId = 1,
  )

  val projectDbm = ProjectDbm(
    projectId = userIdDbm.projectId,
    type = "OPENSOURCE",
    status = "ACTIVE",
    userIdType = "USERNAME",
    name = "name",
    description = "description",
    logoUrl = "logoUrl",
    websiteUrl = "websiteUrl",
    maxUsers = 1000,
    anyoneCanSearch = true,
    onHold = true,
    languageTag = Locale.US.toLanguageTag(),
    requiresSignupCodes = false,
    maxSignupCodesPerUser = Integer.MAX_VALUE,
    mailgunApiKey = mailgunConfig.apiKey,
    mailgunDomain = mailgunConfig.domain,
    mailgunSenderName = mailgunConfig.senderName,
    mailgunSenderEmail = mailgunConfig.senderEmail,
    twilioAccountSid = twilioConfig.accountSid,
    twilioAuthToken = twilioConfig.authToken,
    twilioMessagingServiceId = twilioConfig.messagingServiceId,
    twilioMaxPricePerMessage = twilioConfig.maxPricePerMessage,
    twilioMaxRetryAttempts = twilioConfig.maxRetryAttempts,
    twilioDefaultSenderName = twilioConfig.defaultSenderName,
    twilioDefaultSenderNumber = twilioConfig.defaultSenderNumber,
    firebaseProjectName = firebaseConfig.projectName,
    firebaseServiceAccountKeyJsonBase64 = firebaseConfig.serviceAccountKeyJsonBase64,
    createdAt = Date(0xC20000),
    updatedAt = Date(0xA20000),
  )

  val companyDbm = OrganizationDbm(
    name = "Company",
    street = "Street Name 1",
    postcode = "12345",
    city = "City",
    countryCode = "DE",
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val companyUpdatedDbm = OrganizationDbm(
    name = "Company 1",
    street = "Street Name 11",
    postcode = "123451",
    city = "City 1",
    countryCode = "DF",
  )

  val userDbm = UserDbm(
    id = userIdDbm,
    project = projectDbm,
    signature = "drowssap",
    name = "User's Name",
    type = "ORGANIZATION",
    authority = "ADMIN",
    allowsSpam = true,
    contact = "user@example.com",
    contactType = "EMAIL",
    verificationToken = "abcd1234",
    birthday = Date(0xB00000),
    company = companyDbm,
    languageTag = Locale.US.toLanguageTag(),
    createdAt = Date(0xC00000),
    updatedAt = Date(0xA00000),
  )

  val userUpdatedDbm = UserDbm(
    id = userIdDbm,
    project = projectDbm,
    signature = "1drowssap",
    name = "User's Name 1",
    type = "PERSONAL",
    authority = "MODERATOR",
    allowsSpam = false,
    contact = "+491760000001",
    contactType = "PHONE",
    verificationToken = "abcd12341",
    birthday = Date(0x10B00001),
    company = companyUpdatedDbm,
    languageTag = Locale.UK.toLanguageTag(),
    createdAt = userDbm.createdAt,
    updatedAt = Date(0xA00001),
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val projectCreationKeyDbm = ProjectCreationKeyDbm(
    creatorUserId = user.id.userId,
    creatorProjectId = user.id.projectId,
    createdProjectId = project.id,
  )

  val projectCreationDbm = ProjectCreationDbm(
    data = projectCreationKeyDbm,
    user = userDbm,
    project = projectDbm,
  )

  val tokenDetailsDbm = TokenDetailsDbm(
    tokenValue = tokenValue,
    blocked = true,
    owner = userDbm,
  )

  val schemaDbm = SchemaDbm(
    version = 1,
    isInitialized = true,
  )

  val messageTemplateDbm = MessageTemplateDbm(
    id = 10,
    project = projectDbm,
    name = "template",
    languageTag = Locale.US.toLanguageTag(),
    title = "title",
    content = "content",
    isHtml = true,
    createdAt = Date(0x10000E),
    updatedAt = Date(0x1F0000),
  )

  val pushDeviceDbm = PushDeviceDbm(
    deviceId = pushDevice.deviceId,
    type = PushDevice.Type.ANDROID.name,
    owner = userDbm,
  )

  val signupCodeDbm = SignupCodeDbm(
    code = "code",
    isUsed = false,
    owner = userDbm,
    createdAt = Date(0x10000E),
    usedAt = null,
  )

  // endregion

  // region Network Models

  val companyDto = OrganizationDto(
    name = "Company",
    street = "Street Name 1",
    postcode = "12345",
    city = "City",
    countryCode = "DE",
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val companyDtoUpdated = OrganizationDto(
    name = "Company 1",
    street = "Street Name 11",
    postcode = "123451",
    city = "City 1",
    countryCode = "DF",
  )

  val userResponse = UserResponse(
    userId = userId.userId,
    projectId = userId.projectId,
    universalId = universalUserId,
    name = "User's Name",
    type = "ORGANIZATION",
    authority = "ADMIN",
    allowsSpam = true,
    contact = "user@example.com",
    contactType = "EMAIL",
    birthday = "1970-05-14",
    company = companyDto,
    languageTag = Locale.US.toLanguageTag(),
    createdAt = "1970-05-14 03:04",
    updatedAt = "1970-05-15 05:06",
  )

  val userResponseUpdated = UserResponse(
    userId = userId.userId,
    projectId = userId.projectId,
    universalId = universalUserId,
    name = "User's Name 1",
    type = User.Type.PERSONAL.name,
    authority = User.Authority.MODERATOR.name,
    allowsSpam = false,
    contact = "+491760000001",
    contactType = User.ContactType.PHONE.name,
    birthday = "1978-11-15",
    company = companyDtoUpdated,
    languageTag = Locale.UK.toLanguageTag(),
    createdAt = "1970-05-14 03:04",
    updatedAt = "1970-05-02 08:42",
  )

  val tokenResponse = TokenResponse(
    tokenValue = tokenValue,
  )

  val tokenDetailsResponse = TokenDetailsResponse(
    tokenValue = tokenValue,
    ownerId = userId.userId,
    ownerProjectId = userId.projectId,
    ownerUniversalId = universalUserId,
    createdAt = "2021-05-01 19:35",
    expiresAt = "2026-10-22 19:35",
    authority = "ADMIN",
    isBlocked = true,
    origin = "Token Origin",
    ipAddress = ipAddress,
    geo = geoMerged,
    isStatic = false,
  )

  val userCredentialsRequest = UserCredentialsRequest(
    universalId = universalUserId,
    signature = "password",
    origin = "Token Origin",
  )

  val creatorCredentialsRequest = CreatorCredentialsRequest(
    universalId = userCredentialsRequest.universalId,
    signature = userCredentialsRequest.signature,
    origin = userCredentialsRequest.origin,
  )

  val projectFeatureResponse = ProjectFeatureResponse(
    name = Feature.BASIC.name,
    isRequired = true,
  )

  val projectStateResponse = ProjectStateResponse(
    status = project.status.name,
    usableFeatures = listOf(projectFeatureResponse),
    unusableFeatures = emptyList(),
  )

  private val mailgunConfigDto = MailgunConfigDto(
    apiKey = mailgunConfig.apiKey,
    domain = mailgunConfig.domain,
    senderName = mailgunConfig.senderName,
    senderEmail = mailgunConfig.senderEmail,
  )

  private val mailgunConfigDtoUpdated = MailgunConfigDto(
    apiKey = mailgunConfigUpdated.apiKey,
    domain = mailgunConfigUpdated.domain,
    senderName = mailgunConfigUpdated.senderName,
    senderEmail = mailgunConfigUpdated.senderEmail,
  )

  private val twilioConfigDto = TwilioConfigDto(
    accountSid = twilioConfig.accountSid,
    authToken = twilioConfig.authToken,
    messagingServiceId = twilioConfig.messagingServiceId,
    maxPricePerMessage = twilioConfig.maxPricePerMessage,
    maxRetryAttempts = twilioConfig.maxRetryAttempts,
    defaultSenderName = twilioConfig.defaultSenderName.trim(),
    defaultSenderNumber = twilioConfig.defaultSenderNumber,
  )

  private val twilioConfigDtoUpdated = TwilioConfigDto(
    accountSid = twilioConfigUpdated.accountSid,
    authToken = twilioConfigUpdated.authToken,
    messagingServiceId = twilioConfigUpdated.messagingServiceId,
    maxPricePerMessage = twilioConfigUpdated.maxPricePerMessage,
    maxRetryAttempts = twilioConfigUpdated.maxRetryAttempts,
    defaultSenderName = twilioConfigUpdated.defaultSenderName.trim(),
    defaultSenderNumber = twilioConfigUpdated.defaultSenderNumber,
  )

  private val firebaseConfigDto = FirebaseConfigDto(
    projectName = firebaseConfig.projectName,
    serviceAccountKeyJsonBase64 = firebaseConfig.serviceAccountKeyJsonBase64,
  )

  private val firebaseConfigDtoUpdated = FirebaseConfigDto(
    projectName = firebaseConfigUpdated.projectName,
    serviceAccountKeyJsonBase64 = firebaseConfigUpdated.serviceAccountKeyJsonBase64,
  )

  val projectResponse = ProjectResponse(
    projectId = userId.projectId,
    type = project.type.name,
    state = projectStateResponse,
    userIdType = project.userIdType.name,
    name = project.name,
    description = project.description,
    logoUrl = project.logoUrl,
    websiteUrl = project.websiteUrl,
    maxUsers = project.maxUsers,
    anyoneCanSearch = project.anyoneCanSearch,
    onHold = project.onHold,
    languageTag = Locale.US.toLanguageTag(),
    requiresSignupCodes = project.requiresSignupCodes,
    maxSignupCodesPerUser = project.maxSignupCodesPerUser,
    mailgunConfig = mailgunConfigDto,
    twilioConfig = twilioConfigDto,
    firebaseConfig = firebaseConfigDto,
    createdAt = "1970-01-01 03:31",
    updatedAt = "1970-01-01 02:56",
  )

  val variableResponse = VariableResponse(
    code = Variable.USER_NAME.code,
    example = Variable.USER_NAME.example,
  )

  val messageTemplateResponse = MessageTemplateResponse(
    id = messageTemplate.id,
    name = messageTemplate.name,
    languageTag = messageTemplate.languageTag,
    title = messageTemplate.title,
    content = messageTemplate.content,
    isHtml = messageTemplate.isHtml,
    createdAt = "1970-01-01 00:17",
    updatedAt = "1970-01-01 00:33",
  )

  val messageResponse = MessageResponse(
    template = messageTemplateResponse,
    materialized = "content",
  )

  val pushDeviceResponse = PushDeviceResponse(
    deviceId = pushDevice.deviceId,
    type = pushDevice.type.name,
  )

  val pushDevicesResponse = PushDevicesResponse(
    devices = listOf(pushDeviceResponse),
  )

  val signupCodeResponse = SignupCodeResponse(
    code = signupCode.code,
    isUsed = false,
    createdAt = "1970-01-01 00:17",
    usedAt = null,
  )

  val signupCodesResponse = SignupCodesResponse(
    signupCodes = listOf(signupCodeResponse),
    maxSignupCodes = project.maxSignupCodesPerUser,
  )

  // endregion

  // region Network Ops Models

  val creatorSignupRequest = CreatorSignupRequest(
    userId = "username",
    rawSignature = "password",
    name = "User's Name",
    type = "ORGANIZATION",
    birthday = "1970-05-14",
    company = companyDto,
    signupCode = "FAKE-CODE-1234",
  )

  val companyUpdaterDto = OrganizationUpdaterDto(
    name = SettableRequest("Company 1"),
    street = SettableRequest("Street Name 11"),
    postcode = SettableRequest("123451"),
    city = SettableRequest("City 1"),
    countryCode = SettableRequest("DF"),
  )

  val userSignupRequest = UserSignupRequest(
    userId = "username",
    rawSignature = "password",
    name = "User's Name",
    type = "ORGANIZATION",
    allowsSpam = true,
    contact = "user@example.com",
    contactType = "EMAIL",
    birthday = "1970-05-14",
    company = companyDto,
    languageTag = Locale.US.toLanguageTag(),
    signupCode = "FAKE-CODE-1234",
  )

  val userUpdateAuthorityRequest = UserUpdateAuthorityRequest(authority = User.Authority.MODERATOR.name)

  val userUpdateDataRequest = UserUpdateDataRequest(
    name = SettableRequest("User's Name 1"),
    type = SettableRequest("PERSONAL"),
    allowsSpam = SettableRequest(false),
    contact = SettableRequest("+491760000001"),
    contactType = SettableRequest("PHONE"),
    birthday = SettableRequest("1978-11-15"),
    company = SettableRequest(companyUpdaterDto),
    languageTag = SettableRequest(Locale.UK.toLanguageTag()),
  )

  val userUpdateSignatureRequest = UserUpdateSignatureRequest(
    rawSignatureOld = "password",
    rawSignatureNew = "password1",
  )

  val projectCreateRequest = ProjectCreateRequest(
    type = "OPENSOURCE",
    userIdType = "USERNAME",
    ownerUniversalId = universalUserId,
    name = project.name,
    description = project.description,
    logoUrl = project.logoUrl,
    websiteUrl = project.websiteUrl,
    maxUsers = project.maxUsers,
    anyoneCanSearch = project.anyoneCanSearch,
    languageTag = project.languageTag,
    requiresSignupCodes = project.requiresSignupCodes,
    maxSignupCodesPerUser = project.maxSignupCodesPerUser,
    mailgunConfig = mailgunConfigDto,
    twilioConfig = twilioConfigDto,
    firebaseConfig = firebaseConfigDto,
  )

  val projectUpdateRequest = ProjectUpdateRequest(
    type = SettableRequest("FREE"),
    status = SettableRequest("SUSPENDED"),
    name = SettableRequest("name1"),
    description = SettableRequest("description1"),
    logoUrl = SettableRequest("logoUrl1"),
    websiteUrl = SettableRequest("websiteUrl1"),
    maxUsers = SettableRequest(1001),
    anyoneCanSearch = SettableRequest(false),
    onHold = SettableRequest(false),
    languageTag = SettableRequest(Locale.UK.toLanguageTag()),
    requiresSignupCodes = SettableRequest(true),
    maxSignupCodesPerUser = SettableRequest(5),
    mailgunConfig = SettableRequest(mailgunConfigDtoUpdated),
    twilioConfig = SettableRequest(twilioConfigDtoUpdated),
    firebaseConfig = SettableRequest(firebaseConfigDtoUpdated),
  )

  val messageTemplateCreateRequest = MessageTemplateCreateRequest(
    name = messageTemplateCreator.name,
    languageTag = messageTemplateCreator.languageTag,
    title = messageTemplateCreator.title,
    content = messageTemplateCreator.content,
    isHtml = messageTemplateCreator.isHtml,
  )

  val messageTemplateUpdateRequest = MessageTemplateUpdateRequest(
    name = SettableRequest(messageTemplateUpdater.name!!.value),
    languageTag = SettableRequest(messageTemplateUpdater.languageTag!!.value),
    title = SettableRequest(messageTemplateUpdater.title!!.value),
    content = SettableRequest(messageTemplateUpdater.content!!.value),
    isHtml = SettableRequest(messageTemplateUpdater.isHtml!!.value),
  )

  val messageInputsRequest = MessageInputsRequest(
    universalUserId = messageInputs.userId?.toUniversalFormat(),
    projectId = messageInputs.projectId,
  )

  val pushDeviceRequest = PushDeviceRequest(
    deviceId = pushDevice.deviceId,
    type = pushDevice.type.name,
  )

  val messageSendRequest = MessageSendRequest(
    type = CommunicationsService.Type.EMAIL.name,
    templateId = messageTemplate.id,
    templateName = messageTemplate.name,
  )

  // endregion

}
