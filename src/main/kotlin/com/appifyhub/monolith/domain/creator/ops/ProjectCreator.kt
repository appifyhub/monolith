package com.appifyhub.monolith.domain.creator.ops

import com.appifyhub.monolith.domain.creator.Project.Status
import com.appifyhub.monolith.domain.creator.Project.Type
import com.appifyhub.monolith.domain.creator.Project.UserIdType
import com.appifyhub.monolith.domain.user.User

data class ProjectCreator(
  val owner: User?,
  val type: Type,
  val status: Status,
  val userIdType: UserIdType,
  val name: String,
  val description: String?,
  val logoUrl: String?,
  val websiteUrl: String?,
  val maxUsers: Int,
  val anyoneCanSearch: Boolean,
  val onHold: Boolean,
  val languageTag: String?,
)
