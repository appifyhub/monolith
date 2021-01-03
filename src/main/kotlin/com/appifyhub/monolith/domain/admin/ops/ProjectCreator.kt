package com.appifyhub.monolith.domain.admin.ops

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project.Status
import com.appifyhub.monolith.domain.admin.Project.Type

data class ProjectCreator(
  val id: Long?,
  val account: Account,
  val name: String,
  val type: Type = Type.COMMERCIAL,
  val status: Status = Status.REVIEW,
)