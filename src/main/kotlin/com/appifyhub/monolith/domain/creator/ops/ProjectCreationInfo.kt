package com.appifyhub.monolith.domain.creator.ops

import com.appifyhub.monolith.domain.creator.Project.Status
import com.appifyhub.monolith.domain.creator.Project.Type
import com.appifyhub.monolith.domain.creator.Project.UserIdType

data class ProjectCreationInfo(
  val type: Type = Type.COMMERCIAL,
  val status: Status = Status.REVIEW,
  val userIdType: UserIdType = UserIdType.RANDOM,
)
