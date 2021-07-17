package com.appifyhub.monolith.util

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.domain.admin.property.PropertyCategory
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration
import com.appifyhub.monolith.domain.admin.property.PropertyTag
import com.appifyhub.monolith.domain.admin.property.PropertyType
import com.appifyhub.monolith.domain.admin.property.ops.PropertyFilter
import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.auth.ops.TokenCreator
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.geo.Geolocation
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.network.admin.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.admin.property.PropertyResponse
import com.appifyhub.monolith.network.admin.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.auth.AdminCredentialsRequest
import com.appifyhub.monolith.network.auth.TokenDetailsResponse
import com.appifyhub.monolith.network.auth.TokenResponse
import com.appifyhub.monolith.network.auth.UserCredentialsRequest
import com.appifyhub.monolith.network.common.SettableRequest
import com.appifyhub.monolith.network.user.OrganizationDto
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.network.user.ops.OrganizationUpdaterDto
import com.appifyhub.monolith.network.user.ops.UserCreatorRequest
import com.appifyhub.monolith.network.user.ops.UserUpdaterRequest
import com.appifyhub.monolith.security.JwtClaims
import com.appifyhub.monolith.security.JwtHelper.Claims
import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import com.appifyhub.monolith.storage.model.admin.PropertyDbm
import com.appifyhub.monolith.storage.model.admin.PropertyIdDbm
import com.appifyhub.monolith.storage.model.auth.TokenDetailsDbm
import com.appifyhub.monolith.storage.model.schema.SchemaDbm
import com.appifyhub.monolith.storage.model.user.OrganizationDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm
import java.util.Date
import java.util.concurrent.TimeUnit

@Suppress("MemberVisibilityCanBePrivate", "unused", "MayBeConstant")
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

  val universalUserId = "username$2"

  val userId = UserId(
    userId = "username",
    projectId = 2,
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
    accountId = 1,
    isStatic = false,
  )

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
    accountId = 1,
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
    createdAt = Date(0xC00000),
    updatedAt = Date(0xA00000),
    company = company,
    ownedTokens = emptyList(), // updated below
    account = null, // updated below
  )

  val account = Account(
    id = 1,
    owners = emptyList(), // updated below
    createdAt = Date(0xC10000),
    updatedAt = Date(0xA10000),
  ).let { account ->
    // amazing hacks!
    user = user.copy(
      account = account,
      ownedTokens = listOf(tokenDetails),
    )
    account.copy(owners = listOf(user))
  }

  val project = Project(
    id = userId.projectId,
    account = account,
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
    config = PropertyConfiguration.GENERIC_STRING,
    projectId = project.id,
    rawValue = "value",
    updatedAt = Date(0xFF0000),
  )

  val propInteger = Property.IntegerProp(
    config = PropertyConfiguration.GENERIC_INTEGER,
    projectId = project.id,
    rawValue = "1",
    updatedAt = Date(0xFF0000),
  )

  val propDecimal = Property.DecimalProp(
    config = PropertyConfiguration.GENERIC_DECIMAL,
    projectId = project.id,
    rawValue = "1.1",
    updatedAt = Date(0xFF0000),
  )

  val propFlag = Property.FlagProp(
    config = PropertyConfiguration.GENERIC_FLAG,
    projectId = project.id,
    rawValue = "true",
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

  // endregion

  // region Data Models

  val userIdDbm = UserIdDbm(
    userId = "username",
    projectId = 2,
  )

  val accountDbm = AccountDbm(
    accountId = 1,
    createdAt = Date(0xC10000),
    updatedAt = Date(0xA10000),
  )

  val projectDbm = ProjectDbm(
    projectId = userIdDbm.projectId,
    account = accountDbm,
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
    createdAt = Date(0xC00000),
    updatedAt = Date(0xA00000),
    company = companyDbm,
    account = accountDbm,
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

  val propDecimalIdDbm = PropertyIdDbm(propDecimal.config.name, propDecimal.projectId)

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

  var tokenDetailsUpdated = TokenDetails(
    tokenValue = tokenValue + "1",
    isBlocked = true,
    createdAt = Date(0xC30001),
    expiresAt = Date(0xE00001),
    ownerId = userId,
    authority = User.Authority.ADMIN,
    origin = "Token Origin 1",
    ipAddress = ipAddress.reversed(),
    geo = "$geoMerged 1",
    accountId = 1,
    isStatic = false,
  )

  val companyUpdated = company.copy(
    name = "Company 1",
    street = "Street Name 11",
    postcode = "123451",
    city = "City 1",
    countryCode = "DF",
  )

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
    createdAt = user.createdAt,
    updatedAt = Date(0xA00001),
    ownedTokens = emptyList(), // updated below
    account = null, // updated below
  )

  val accountUpdated = account.copy(
    updatedAt = Date(0xA10001),
  ).let { account ->
    // amazing hacks! again!
    userUpdated = userUpdated.copy(
      account = account,
      ownedTokens = listOf(tokenDetailsUpdated),
    )
    account.copy(owners = listOf(userUpdated))
  }

  val accountUpdatedDbm = AccountDbm(
    accountId = account.id,
    createdAt = accountDbm.createdAt,
    updatedAt = Date(0xA10001),
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
    createdAt = userDbm.createdAt,
    updatedAt = Date(0xA00001),
    company = companyUpdatedDbm,
    account = accountUpdatedDbm,
  )

  val projectUpdated = Project(
    id = project.id,
    account = accountUpdated,
    type = Project.Type.FREE,
    status = Project.Status.SUSPENDED,
    userIdType = project.userIdType,
    createdAt = project.createdAt,
    updatedAt = Date(0xA20001),
  )

  val projectUpdatedDbm = ProjectDbm(
    projectId = project.id,
    account = accountUpdatedDbm,
    type = "FREE",
    status = "SUSPENDED",
    userIdType = projectDbm.userIdType,
    createdAt = projectDbm.createdAt,
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
    Claims.ACCOUNT_ID to account.id,
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
    Claims.ACCOUNT_ID to account.id,
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
    account = Settable(accountUpdated),
  )

  val projectCreator = ProjectCreator(
    account = account,
    type = Project.Type.OPENSOURCE,
    status = Project.Status.ACTIVE,
    userIdType = Project.UserIdType.USERNAME,
  )

  val projectUpdater = ProjectUpdater(
    id = project.id,
    account = Settable(accountUpdated),
    type = Settable(Project.Type.FREE),
    status = Settable(Project.Status.SUSPENDED),
  )

  val accountUpdater = AccountUpdater(
    id = account.id,
    addedOwners = Settable(emptyList()),
    removedOwners = Settable(emptyList()),
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

  val adminCredentialsRequest = AdminCredentialsRequest(
    universalId = universalUserId,
    secret = "password",
    origin = "Token Origin",
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
    name = PropertyConfiguration.GENERIC_STRING.name,
    type = PropertyConfiguration.GENERIC_STRING.type.name,
    category = PropertyConfiguration.GENERIC_STRING.category.name,
    tags = PropertyConfiguration.GENERIC_STRING.tags.map(PropertyTag::name).toSet(),
    defaultValue = PropertyConfiguration.GENERIC_STRING.defaultValue,
    isMandatory = PropertyConfiguration.GENERIC_STRING.isMandatory,
    isSecret = PropertyConfiguration.GENERIC_STRING.isSecret,
    isDeprecated = PropertyConfiguration.GENERIC_STRING.isDeprecated,
  )

  val propertyResponse = PropertyResponse(
    config = propertyConfigurationResponse,
    rawValue = propString.rawValue,
    updatedAt = "1970-01-01 04:38",
  )

  // endregion

  // region Network Ops Models

  val userCreatorRequest = UserCreatorRequest(
    userId = "username",
    rawSignature = "password",
    name = "User's Name",
    type = "ORGANIZATION",
    authority = "ADMIN",
    allowsSpam = true,
    contact = "user@example.com",
    contactType = "EMAIL",
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

  val userUpdaterRequest = UserUpdaterRequest(
    rawSignature = SettableRequest("password1"),
    type = SettableRequest("PERSONAL"),
    authority = SettableRequest("MODERATOR"),
    contactType = SettableRequest("PHONE"),
    allowsSpam = SettableRequest(false),
    name = SettableRequest("User's Name 1"),
    contact = SettableRequest("+491760000001"),
    birthday = SettableRequest("1970-05-15"),
    company = SettableRequest(companyUpdaterDto),
  )

  // endregion

  // region Network Models Updated

  val companyDtoUpdated = companyDto.copy(
    name = "Company 1",
    street = "Street Name 11",
    postcode = "123451",
    city = "City 1",
    countryCode = "DF",
  )

  // endregion

}
