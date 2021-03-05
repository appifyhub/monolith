package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.common.applySettable
import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import com.appifyhub.monolith.util.TimeProvider
import org.springframework.security.crypto.password.PasswordEncoder

fun AccountUpdater.applyTo(
  account: Account,
  timeProvider: TimeProvider,
): Account = account
  .applySettable(addedOwners) { copy(owners = owners + it) }
  .applySettable(removedOwners) { removedList ->
    copy(owners = owners.filter { it !in removedList })
  }
  .copy(updatedAt = timeProvider.currentDate)

fun ProjectUpdater.applyTo(
  project: Project,
  timeProvider: TimeProvider,
): Project = project
  .applySettable(account) { copy(account = it) }
  .applySettable(name) { copy(name = it) }
  .applySettable(type) { copy(type = it) }
  .applySettable(status) { copy(status = it) }
  .copy(updatedAt = timeProvider.currentDate)

fun ProjectCreator.toProjectData(
  rawSignature: String,
  passwordEncoder: PasswordEncoder,
  timeProvider: TimeProvider,
): ProjectDbm = ProjectDbm(
  projectId = null,
  account = account.toData(),
  signature = passwordEncoder.encode(rawSignature),
  name = name,
  type = type.name,
  status = status.name,
  userIdType = userIdType.name,
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
)

fun AccountDbm.toDomain(): Account = Account(
  id = accountId!!,
  owners = emptyList(),
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Account.toData(): AccountDbm = AccountDbm(
  accountId = id,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun ProjectDbm.toDomain(): Project = Project(
  id = projectId!!,
  account = account.toDomain(),
  signature = signature,
  name = name,
  type = Project.Type.find(type, default = Project.Type.COMMERCIAL),
  status = Project.Status.find(status, default = Project.Status.REVIEW),
  userIdType = Project.UserIdType.find(userIdType, default = Project.UserIdType.RANDOM),
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Project.toData(): ProjectDbm = ProjectDbm(
  projectId = id,
  account = account.toData(),
  signature = signature,
  name = name,
  type = type.name,
  status = status.name,
  userIdType = userIdType.name,
  createdAt = createdAt,
  updatedAt = updatedAt,
)