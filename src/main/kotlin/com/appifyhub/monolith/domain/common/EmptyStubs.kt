package com.appifyhub.monolith.domain.common

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import java.util.Date
import java.util.Locale

fun stubProject() = Project(
  id = -1,
  type = Project.Type.COMMERCIAL,
  status = Project.Status.REVIEW,
  userIdType = Project.UserIdType.RANDOM,
  languageTag = Locale.US.toLanguageTag(),
  createdAt = Date(),
  updatedAt = Date(),
)

fun stubUserId() = UserId(
  userId = "id",
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
  id = stubUserId(),
  signature = "signature",
  name = "name",
  type = User.Type.PERSONAL,
  authority = User.Authority.DEFAULT,
  allowsSpam = false,
  contact = "contact",
  contactType = User.ContactType.CUSTOM,
  verificationToken = "verificationToken",
  birthday = Date(),
  company = stubOrganization(),
  languageTag = null,
  createdAt = Date(),
  updatedAt = Date(),
)
