package com.appifyhub.monolith.domain.common

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.schema.Schema
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import java.util.Date

fun stubAccount() = Account(
  id = -1,
  owners = emptyList(),
  createdAt = Date(),
  updatedAt = Date(),
)

fun stubProject() = Project(
  id = -1,
  account = stubAccount(),
  signature = "signature",
  name = "name",
  type = Project.Type.COMMERCIAL,
  status = Project.Status.REVIEW,
  userIdType = Project.UserIdType.RANDOM,
  createdAt = Date(),
  updatedAt = Date(),
)

fun stubUserId() = UserId(
  id = "id",
  projectId = -1,
)

fun stubOrganization() = Organization(
  name = "name",
  street = "street",
  postcode = "postcode",
  city = "city",
  countryCode = "countryCode",
)

fun stubUser() = User(
  userId = stubUserId(),
  signature = "signature",
  name = "name",
  type = User.Type.PERSONAL,
  authority = User.Authority.DEFAULT,
  allowsSpam = false,
  contact = "contact",
  contactType = User.ContactType.CUSTOM,
  verificationToken = "verificationToken",
  birthday = Date(),
  createdAt = Date(),
  updatedAt = Date(),
  company = stubOrganization(),
  ownedTokens = emptyList(),
  account = stubAccount(),
)

fun stubToken() = Token(
  tokenLocator = "token",
)

fun stubOwnedToken() = OwnedToken(
  token = stubToken(),
  isBlocked = false,
  origin = null,
  createdAt = Date(),
  expiresAt = Date(),
  owner = stubUser(),
)

fun stubSchema() = Schema(
  version = -1,
  isInitialized = false,
)