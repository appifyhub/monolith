package com.appifyhub.monolith.domain.admin.ops

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project.Status
import com.appifyhub.monolith.domain.admin.Project.Type
import com.appifyhub.monolith.domain.common.Settable

data class ProjectUpdater(
  val id: Long,
  val account: Settable<Account>? = null,
  val type: Settable<Type>? = null,
  val status: Settable<Status>? = null,
)
