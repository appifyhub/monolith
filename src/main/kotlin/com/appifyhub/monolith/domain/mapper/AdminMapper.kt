package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountOwnerUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.domain.admin.property.Property.DecimalProp
import com.appifyhub.monolith.domain.admin.property.Property.FlagProp
import com.appifyhub.monolith.domain.admin.property.Property.IntegerProp
import com.appifyhub.monolith.domain.admin.property.Property.StringProp
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration
import com.appifyhub.monolith.domain.admin.property.PropertyType.DECIMAL
import com.appifyhub.monolith.domain.admin.property.PropertyType.FLAG
import com.appifyhub.monolith.domain.admin.property.PropertyType.INTEGER
import com.appifyhub.monolith.domain.admin.property.PropertyType.STRING
import com.appifyhub.monolith.domain.common.applySettable
import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import com.appifyhub.monolith.storage.model.admin.PropertyDbm
import com.appifyhub.monolith.storage.model.admin.PropertyIdDbm
import com.appifyhub.monolith.util.TimeProvider
import java.util.Date

fun AccountOwnerUpdater.applyTo(
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
  timeProvider: TimeProvider,
): ProjectDbm = ProjectDbm(
  projectId = null,
  account = account.toData(),
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
  name = name,
  type = type.name,
  status = status.name,
  userIdType = userIdType.name,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Property.Companion.instantiate(
  config: PropertyConfiguration,
  projectId: Long,
  rawValue: String,
  updatedAt: Date,
): Property<*> = when (config.type) {
  STRING -> StringProp(config, projectId, rawValue, updatedAt)
  INTEGER -> IntegerProp(config, projectId, rawValue, updatedAt)
  DECIMAL -> DecimalProp(config, projectId, rawValue, updatedAt)
  FLAG -> FlagProp(config, projectId, rawValue, updatedAt)
}

fun PropertyDbm.toDomain(): Property<*> = Property.instantiate(
  config = PropertyConfiguration.find(name = id.name)!!,
  projectId = id.projectId,
  rawValue = rawValue,
  updatedAt = updatedAt,
)

fun Property<*>.toData(
  project: Project,
): PropertyDbm = PropertyDbm(
  id = PropertyIdDbm(name = config.name, projectId = project.id),
  project = project.toData(),
  rawValue = rawValue,
  updatedAt = updatedAt,
)

@Suppress("UNCHECKED_CAST")
fun <T : Any> Property<*>.withCast(): Property<T> = this as Property<T>
