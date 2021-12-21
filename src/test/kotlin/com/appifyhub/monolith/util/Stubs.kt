package com.appifyhub.monolith.util

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.auth.ops.TokenCreator
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.PropertyCategory
import com.appifyhub.monolith.domain.creator.property.PropertyTag
import com.appifyhub.monolith.domain.creator.property.PropertyType
import com.appifyhub.monolith.domain.creator.property.ops.PropertyFilter
import com.appifyhub.monolith.domain.creator.setup.ProjectStatus
import com.appifyhub.monolith.domain.geo.Geolocation
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.network.auth.CreatorCredentialsRequest
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.auth.UserCredentialsRequest
import com.appifyhub.monolith.network.common.SettableRequest
import com.appifyhub.monolith.network.creator.project.ProjectFeatureDto
import com.appifyhub.monolith.network.creator.project.ProjectResponse
import com.appifyhub.monolith.network.creator.project.ProjectStatusDto
import com.appifyhub.monolith.network.creator.project.ops.ProjectCreateRequest
import com.appifyhub.monolith.network.creator.project.ops.ProjectUpdateRequest
import com.appifyhub.monolith.network.creator.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.creator.property.PropertyResponse
import com.appifyhub.monolith.network.creator.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.creator.user.ops.CreatorSignupRequest
import com.appifyhub.monolith.network.user.OrganizationDto
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.network.user.ops.OrganizationUpdaterDto
import com.appifyhub.monolith.network.user.ops.UserSignupRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateAuthorityRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateDataRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateSignatureRequest
import com.appifyhub.monolith.network.user.ops.UserUpdateVerificationRequest
import com.appifyhub.monolith.security.JwtClaims
import com.appifyhub.monolith.security.JwtHelper.Claims
import com.appifyhub.monolith.service.access.AccessManager.Feature
import com.appifyhub.monolith.storage.model.auth.TokenDetailsDbm
import com.appifyhub.monolith.storage.model.creator.ProjectCreationDbm
import com.appifyhub.monolith.storage.model.creator.ProjectCreationKeyDbm
import com.appifyhub.monolith.storage.model.creator.ProjectDbm
import com.appifyhub.monolith.storage.model.creator.PropertyDbm
import com.appifyhub.monolith.storage.model.creator.PropertyIdDbm
import com.appifyhub.monolith.storage.model.schema.SchemaDbm
import com.appifyhub.monolith.storage.model.user.OrganizationDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Suppress("MayBeConstant")
object Stubs {

  // region Auth Models

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

  val project = Project(
    id = userId.projectId,
    type = Project.Type.OPENSOURCE,
    status = Project.Status.ACTIVE,
    userIdType = Project.UserIdType.USERNAME,
    createdAt = Date(0xC20000),
    updatedAt = Date(0xA20000),
  )

  val schema = Schema(
    version = 1,
    isInitialized = true,
  )

  val propString = Property.StringProp(
    config = ProjectProperty.GENERIC_STRING,
    projectId = project.id,
    rawValue = "value",
    updatedAt = Date(0xFF0000),
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val propStringName = Property.StringProp(
    config = ProjectProperty.NAME,
    projectId = project.id,
    rawValue = "name",
    updatedAt = Date(0xFF0000),
  )

  val propInteger = Property.IntegerProp(
    config = ProjectProperty.GENERIC_INTEGER,
    projectId = project.id,
    rawValue = "1",
    updatedAt = Date(0xFF0000),
  )

  val propDecimal = Property.DecimalProp(
    config = ProjectProperty.GENERIC_DECIMAL,
    projectId = project.id,
    rawValue = "1.1",
    updatedAt = Date(0xFF0000),
  )

  val propFlag = Property.FlagProp(
    config = ProjectProperty.GENERIC_FLAG,
    projectId = project.id,
    rawValue = "true",
    updatedAt = Date(0xFF0000),
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val propFlagOnHold = Property.FlagProp(
    config = ProjectProperty.ON_HOLD,
    projectId = project.id,
    rawValue = "false",
    updatedAt = Date(0xFF0000),
  )

  val propertyFilter = PropertyFilter(
    type = PropertyType.STRING,
    category = PropertyCategory.GENERIC,
    nameContains = "_STRING",
    isMandatory = true,
    isSecret = true,
    isDeprecated = true,
    mustHaveTags = setOf(PropertyTag.GENERIC),
    hasAtLeastOneOfTags = setOf(PropertyTag.GENERIC),
  )

  val projectStatus = ProjectStatus(
    status = Project.Status.ACTIVE,
    usableFeatures = listOf(Feature.BASIC),
    unusableFeatures = emptyList(),
    properties = listOf(propStringName, propFlagOnHold),
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

  val propStringIdDbm = PropertyIdDbm(propString.config.name, propString.projectId)

  val propIntegerIdDbm = PropertyIdDbm(propInteger.config.name, propInteger.projectId)

  @Suppress("MemberVisibilityCanBePrivate")
  val propDecimalIdDbm = PropertyIdDbm(propDecimal.config.name, propDecimal.projectId)

  @Suppress("MemberVisibilityCanBePrivate")
  val propFlagIdDbm = PropertyIdDbm(propFlag.config.name, propFlag.projectId)

  val propStringDbm = PropertyDbm(
    id = propStringIdDbm,
    project = projectDbm,
    rawValue = propString.rawValue,
    updatedAt = propString.updatedAt,
  )

  val propIntegerDbm = PropertyDbm(
    id = propIntegerIdDbm,
    project = projectDbm,
    rawValue = propInteger.rawValue,
    updatedAt = propInteger.updatedAt,
  )

  val propDecimalDbm = PropertyDbm(
    id = propDecimalIdDbm,
    project = projectDbm,
    rawValue = propDecimal.rawValue,
    updatedAt = propDecimal.updatedAt,
  )

  val propFlagDbm = PropertyDbm(
    id = propFlagIdDbm,
    project = projectDbm,
    rawValue = propFlag.rawValue,
    updatedAt = propFlag.updatedAt,
  )

  // endregion

  // region Domain Models (with updated values)

  @Suppress("MemberVisibilityCanBePrivate")
  val companyUpdated = company.copy(
    name = "Company 1",
    street = "Street Name 11",
    postcode = "123451",
    city = "City 1",
    countryCode = "DF",
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val companyUpdatedDbm = OrganizationDbm(
    name = "Company 1",
    street = "Street Name 11",
    postcode = "123451",
    city = "City 1",
    countryCode = "DF",
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

  val projectUpdated = Project(
    id = project.id,
    type = Project.Type.FREE,
    status = Project.Status.SUSPENDED,
    userIdType = project.userIdType,
    createdAt = project.createdAt,
    updatedAt = Date(0xA20001),
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

  // region Domain Ops Models

  val userCreator = UserCreator(
    userId = "username",
    projectId = project.id,
    rawSecret = "password",
    name = "User's Name",
    type = User.Type.ORGANIZATION,
    authority = User.Authority.ADMIN,
    allowsSpam = true,
    contact = "user@example.com",
    contactType = User.ContactType.EMAIL,
    birthday = Date(0xB00000),
    company = company,
    languageTag = Locale.US.toLanguageTag(),
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
  )

  val projectUpdater = ProjectUpdater(
    id = project.id,
    type = Settable(Project.Type.FREE),
    status = Settable(Project.Status.SUSPENDED),
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
    birthday = "1970-05-15",
    company = companyDto,
    languageTag = Locale.US.toLanguageTag(),
    createdAt = "1970-05-14 03:04",
    updatedAt = "1970-05-15 05:06",
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
    secret = "password",
    origin = "Token Origin",
  )

  val creatorCredentialsRequest = CreatorCredentialsRequest(
    universalId = userCredentialsRequest.universalId,
    secret = userCredentialsRequest.secret,
    origin = userCredentialsRequest.origin,
  )

  val propertyFilterQueryParams = PropertyFilterQueryParams(
    type = PropertyType.STRING.name,
    category = PropertyCategory.GENERIC.name,
    name_contains = "_STRING",
    mandatory = true,
    secret = true,
    deprecated = true,
    must_have_tags = listOf(PropertyTag.GENERIC.name),
    has_at_least_one_of_tags = listOf(PropertyTag.GENERIC.name),
  )

  val propertyConfigurationResponse = PropertyConfigurationResponse(
    name = ProjectProperty.GENERIC_STRING.name,
    type = ProjectProperty.GENERIC_STRING.type.name,
    category = ProjectProperty.GENERIC_STRING.category.name,
    tags = ProjectProperty.GENERIC_STRING.tags.map(PropertyTag::name).toSet(),
    defaultValue = ProjectProperty.GENERIC_STRING.defaultValue,
    isMandatory = ProjectProperty.GENERIC_STRING.isMandatory,
    isSecret = ProjectProperty.GENERIC_STRING.isSecret,
    isDeprecated = ProjectProperty.GENERIC_STRING.isDeprecated,
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val propertyConfigurationResponseName = PropertyConfigurationResponse(
    name = ProjectProperty.NAME.name,
    type = ProjectProperty.NAME.type.name,
    category = ProjectProperty.NAME.category.name,
    tags = ProjectProperty.NAME.tags.map(PropertyTag::name).toSet(),
    defaultValue = ProjectProperty.NAME.defaultValue,
    isMandatory = ProjectProperty.NAME.isMandatory,
    isSecret = ProjectProperty.NAME.isSecret,
    isDeprecated = ProjectProperty.NAME.isDeprecated,
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val propertyConfigurationResponseOnHold = PropertyConfigurationResponse(
    name = ProjectProperty.ON_HOLD.name,
    type = ProjectProperty.ON_HOLD.type.name,
    category = ProjectProperty.ON_HOLD.category.name,
    tags = ProjectProperty.ON_HOLD.tags.map(PropertyTag::name).toSet(),
    defaultValue = ProjectProperty.ON_HOLD.defaultValue,
    isMandatory = ProjectProperty.ON_HOLD.isMandatory,
    isSecret = ProjectProperty.ON_HOLD.isSecret,
    isDeprecated = ProjectProperty.ON_HOLD.isDeprecated,
  )

  val propertyResponse = PropertyResponse(
    config = propertyConfigurationResponse,
    rawValue = propString.rawValue,
    updatedAt = "1970-01-01 04:38",
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val propertyResponseName = PropertyResponse(
    config = propertyConfigurationResponseName,
    rawValue = "name",
    updatedAt = "1970-01-01 04:38",
  )

  @Suppress("MemberVisibilityCanBePrivate")
  val propertyResponseOnHold = PropertyResponse(
    config = propertyConfigurationResponseOnHold,
    rawValue = "false",
    updatedAt = "1970-01-01 04:38",
  )

  val projectFeatureDto = ProjectFeatureDto(
    name = Feature.BASIC.name,
    isRequired = true,
    properties = listOf(
      ProjectProperty.NAME.name,
      ProjectProperty.ON_HOLD.name,
    ),
  )

  val projectStatusDto = ProjectStatusDto(
    status = project.status.name,
    usableFeatures = listOf(projectFeatureDto),
    unusableFeatures = emptyList(),
    properties = listOf(
      propertyResponseName,
      propertyResponseOnHold,
    ),
  )

  val projectResponse = ProjectResponse(
    projectId = userId.projectId,
    type = project.type.name,
    status = projectStatusDto,
    userIdType = project.userIdType.name,
    createdAt = "1970-01-01 03:31",
    updatedAt = "1970-01-01 02:56",
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
  )

  val userUpdateAuthorityRequest = UserUpdateAuthorityRequest(authority = User.Authority.MODERATOR.name)

  val userUpdateDataRequest = UserUpdateDataRequest(
    name = SettableRequest("User's Name 1"),
    type = SettableRequest("PERSONAL"),
    allowsSpam = SettableRequest(false),
    contact = SettableRequest("+491760000001"),
    contactType = SettableRequest("PHONE"),
    birthday = SettableRequest("1970-05-15"),
    company = SettableRequest(companyUpdaterDto),
    languageTag = SettableRequest(Locale.UK.toLanguageTag()),
  )

  val userUpdateSignatureRequest = UserUpdateSignatureRequest(
    rawSignatureOld = "password",
    rawSignatureNew = "password1",
  )

  val userUpdateVerificationRequest = UserUpdateVerificationRequest(rawVerificationToken = "abcd1234")

  val projectCreateRequest = ProjectCreateRequest(
    type = "OPENSOURCE",
    userIdType = "USERNAME",
    ownerUniversalId = universalUserId,
  )

  val projectUpdateRequest = ProjectUpdateRequest(
    type = SettableRequest("FREE"),
    status = SettableRequest("SUSPENDED"),
  )

  // endregion

}
