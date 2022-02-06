package com.appifyhub.monolith.domain.common

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.user.Organization
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.creator.CreatorService.Companion.DEFAULT_MAX_USERS
import java.util.Date
import java.util.Locale

/*
 * Why this? (you may ask)
 *
 * Well... Spring's ORM is not very nice to complex object relations,
 * so these are some hacks that needed to be done in order to reduce
 * relational complexity. Let's say it's "temporary".
 */

fun stubProject() = Project(
  id = -1,
  type = Project.Type.COMMERCIAL,
  status = Project.Status.REVIEW,
  userIdType = Project.UserIdType.RANDOM,
  name = "PN",
  description = null,
  logoUrl = null,
  websiteUrl = null,
  maxUsers = DEFAULT_MAX_USERS,
  anyoneCanSearch = false,
  onHold = false,
  languageTag = Locale.US.toLanguageTag(),
  createdAt = Date(),
  updatedAt = Date(),
)

fun stubUser() = User(
  id = UserId(
    userId = "id",
    projectId = -1, // stubbed, not used
  ),
  signature = "signature",
  name = "name",
  type = User.Type.PERSONAL,
  authority = User.Authority.DEFAULT,
  allowsSpam = false,
  contact = "contact",
  contactType = User.ContactType.CUSTOM,
  verificationToken = "verificationToken",
  birthday = Date(),
  company = Organization(
    name = "name", // stubbed, not used
    street = "street", // stubbed, not used
    postcode = "postcode", // stubbed, not used
    city = "city", // stubbed, not used
    countryCode = "countryCode", // stubbed, not used
  ),
  languageTag = null,
  createdAt = Date(),
  updatedAt = Date(),
)

fun stubMessageTemplate() = MessageTemplate(
  id = -1,
  projectId = -1,
  name = "name",
  language = "language",
  content = "content",
  isHtml = false,
  bindings = emptyList(),
  createdAt = Date(),
  updatedAt = Date(),
)
