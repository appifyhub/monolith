package com.appifyhub.monolith.util

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.common.stubUser
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import com.appifyhub.monolith.storage.model.auth.OwnedTokenDbm
import com.appifyhub.monolith.storage.model.auth.TokenDbm
import com.appifyhub.monolith.storage.model.schema.SchemaDbm
import com.appifyhub.monolith.storage.model.user.OrganizationDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.storage.model.user.UserIdDbm
import java.util.Date

@Suppress("MemberVisibilityCanBePrivate", "unused")
object Stubs {

  // region Domain Models

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
    token = Token(
      tokenLocator = "locator",
    ),
    isBlocked = true,
    origin = "Token Origin",
    createdAt = Date(0xC30000),
    expiresAt = Date(0xE00000),
    owner = stubUser(), // updated below
  )

  val token = Token(
    tokenLocator = "locator",
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
    signature = "signature",
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
    signature = "signature",
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

  val tokenDbm = TokenDbm(
    tokenLocator = "locator",
  )

  val ownedTokenDbm = OwnedTokenDbm(
    tokenLocator = "locator",
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

}