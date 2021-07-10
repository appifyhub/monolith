package com.appifyhub.monolith.domain.admin.ops

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project.Status
import com.appifyhub.monolith.domain.admin.Project.Type
import com.appifyhub.monolith.domain.admin.Project.UserIdType

data class ProjectCreator(
  val account: Account,
  val type: Type = Type.COMMERCIAL,
  val status: Status = Status.REVIEW,
  val userIdType: UserIdType = UserIdType.RANDOM,
)
