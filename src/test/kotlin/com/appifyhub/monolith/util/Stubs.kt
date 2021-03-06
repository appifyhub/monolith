package com.appifyhub.monolith.util

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.common.stubUser
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
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
import com.appifyhub.monolith.repository.auth.locator.TokenLocator
import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import com.appifyhub.monolith.storage.model.auth.OwnedTokenDbm
import com.appifyhub.monolith.storage.model.auth.TokenDbm
import com.appifyhub.monolith.storage.model.schema.SchemaDbm
import com.appifyhub.monolith.storage.model.user.OrganizationDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm
import java.util.Date

@Suppress("MemberVisibilityCanBePrivate", "unused", "MayBeConstant")
object Stubs {

  // region Auth Models

  @Suppress("SpellCheckingInspection")
  val tokenLocatorEncoded = "MiQjVExQSSMkZFhObGNtNWhiV1U9JCNUTFBJIyRWRzlyWlc0Z1QzSnBaMmx1JCNUTFBJIyQw"

  val token = Token(tokenLocator = tokenLocatorEncoded)

  val tokenLocator = TokenLocator(
    userId = UserId(
      id = "username",
      projectId = 2,
    ),
    origin = "Token Origin",
    timestamp = 0,
  )

  // endregion

  // region Domain Models

  val unifiedUserId = "username$2"

  val userId = UserId(
    id = "username",
    projectId = 2,
  )

  val company = Organization(
    name = "Company",
    street = "Street Name 1",
    postcode = "12345",
    city = "City",
    countryCode = "DE",
  )

  var ownedToken = OwnedToken(
    token = token,
    isBlocked = true,
    origin = "Token Origin",
    createdAt = Date(0xC30000),
    expiresAt = Date(0xE00000),
    owner = stubUser(), // updated below
  )

  var user = User(
    userId = userId,
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
    user = user.copy(account = account)
    ownedToken = ownedToken.copy(owner = user)
    user = user.copy(ownedTokens = listOf(ownedToken))
    ownedToken = ownedToken.copy(owner = user)
    user = user.copy(ownedTokens = listOf(ownedToken))
    account.copy(owners = listOf(user))
  }

  val project = Project(
    id = userId.projectId,
    account = account,
    signature = "erutangis",
    name = "Project's Name",
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

  // endregion

  // region Data Models

  val userIdDbm = UserIdDbm(
    identifier = "username",
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
    signature = "erutangis",
    name = "Project's Name",
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
    userId = userIdDbm,
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

  val tokenDbm = TokenDbm(tokenLocator = tokenLocatorEncoded)

  val ownedTokenDbm = OwnedTokenDbm(
    tokenLocator = tokenLocatorEncoded,
    blocked = true,
    origin = "Token Origin",
    createdAt = Date(0xC30000),
    expiresAt = Date(0xE00000),
    owner = userDbm,
  )

  val schemaDbm = SchemaDbm(
    version = 1,
    isInitialized = true,
  )

  // endregion

  // region Domain Models (with updated values)

  @Suppress("SpellCheckingInspection")
  val tokenLocatorUpdatedEncoded = "MiQjVExQSSMkZFhObGNtNWhiV1U9JCNUTFBJIyRWRzlyWlc0Z1QzSnBaMmx1SURFPSQjVExQSSMkMA=="

  val tokenUpdated = Token(tokenLocator = tokenLocatorUpdatedEncoded)

  var ownedTokenUpdated = OwnedToken(
    token = tokenUpdated,
    isBlocked = true,
    origin = "Token Origin 1",
    createdAt = Date(0xC30001),
    expiresAt = Date(0xE00001),
    owner = stubUser(), // updated below
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
    userId = user.userId,
    signature = "1drowssap",
    name = "User's Name 1",
    type = User.Type.PERSONAL,
    authority = User.Authority.MODERATOR,
    allowsSpam = false,
    contact = "+1234567890",
    contactType = User.ContactType.PHONE,
    verificationToken = "abcd12341",
    birthday = Date(0xB00001),
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
    userUpdated = userUpdated.copy(account = account)
    ownedTokenUpdated = ownedTokenUpdated.copy(owner = userUpdated)
    userUpdated = userUpdated.copy(ownedTokens = listOf(ownedTokenUpdated))
    ownedTokenUpdated = ownedTokenUpdated.copy(owner = userUpdated)
    userUpdated = userUpdated.copy(ownedTokens = listOf(ownedTokenUpdated))
    account.copy(owners = listOf(userUpdated))
  }

  val accountUpdatedDbm = AccountDbm(
    accountId = account.id,
    createdAt = accountDbm.createdAt,
    updatedAt = Date(0xA10001),
  )

  val userUpdatedDbm = UserDbm(
    userId = userIdDbm,
    project = projectDbm,
    signature = "1drowssap",
    name = "User's Name 1",
    type = "PERSONAL",
    authority = "MODERATOR",
    allowsSpam = false,
    contact = "+1234567890",
    contactType = "PHONE",
    verificationToken = "abcd12341",
    birthday = Date(0xB00001),
    createdAt = userDbm.createdAt,
    updatedAt = Date(0xA00001),
    company = companyUpdatedDbm,
    account = accountUpdatedDbm,
  )

  val projectUpdated = Project(
    id = project.id,
    account = accountUpdated,
    signature = "erutangis",
    name = "Project's Name 1",
    type = Project.Type.FREE,
    status = Project.Status.SUSPENDED,
    userIdType = project.userIdType,
    createdAt = project.createdAt,
    updatedAt = Date(0xA20001),
  )

  val projectUpdatedDbm = ProjectDbm(
    projectId = project.id,
    account = accountUpdatedDbm,
    signature = "erutangis",
    name = "Project's Name 1",
    type = "FREE",
    status = "SUSPENDED",
    userIdType = projectDbm.userIdType,
    createdAt = projectDbm.createdAt,
    updatedAt = Date(0xA20001),
  )

  // endregion

  // region Ops Domain Models

  val userCreator = UserCreator(
    id = "username1",
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
    contact = Settable("+1234567890"),
    verificationToken = Settable("abcd12341"),
    birthday = Settable(Date(0xB00001)),
    company = Settable(companyUpdater),
    account = Settable(accountUpdated),
  )

  val projectCreator = ProjectCreator(
    account = account,
    name = "Project's Name",
    type = Project.Type.OPENSOURCE,
    status = Project.Status.ACTIVE,
    userIdType = Project.UserIdType.USERNAME,
  )

  val projectUpdater = ProjectUpdater(
    id = project.id,
    account = Settable(accountUpdated),
    name = Settable("Project's Name 1"),
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
    userId = userId.id,
    projectId = userId.projectId,
    unifiedId = unifiedUserId,
    name = "User's Name",
    type = "ORGANIZATION",
    authority = "ADMIN",
    allowsSpam = true,
    contact = "user@example.com",
    contactType = "EMAIL",
    birthday = "1970-05-15",
    company = companyDto,
    createdAt = "1970-05-14",
    updatedAt = "1970-05-15",
  )

  val tokenResponse = TokenResponse(
    token = token.tokenLocator,
  )

  val tokenDetailsResponse = TokenDetailsResponse(
    ownerId = userId.id,
    ownerProjectId = userId.projectId,
    ownerUnifiedId = unifiedUserId,
    tokenId = token.tokenLocator,
    isBlocked = true,
    origin = "Token Origin",
    createdAt = "1970-05-28",
    expiresAt = "1970-06-19",
  )

  val userCredentialsRequest = UserCredentialsRequest(
    userId = "username",
    secret = "password",
    origin = "Token Origin",
  )

  val adminCredentialsRequest = AdminCredentialsRequest(
    userId = "username",
    secret = "password",
    origin = "Token Origin",
  )

  // endregion

  // region Network Ops Models

  val userCreatorRequest = UserCreatorRequest(
    id = "username",
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
    contact = SettableRequest("+1234567890"),
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