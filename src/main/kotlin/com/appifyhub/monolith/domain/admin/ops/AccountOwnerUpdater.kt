package com.appifyhub.monolith.domain.admin.ops

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User

data class AccountOwnerUpdater(
  val id: Long,
  val addedOwners: Settable<List<User>>? = null,
  val removedOwners: Settable<List<User>>? = null,
)
