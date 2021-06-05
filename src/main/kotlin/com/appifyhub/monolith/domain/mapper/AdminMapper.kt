package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
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

fun PropertyDbm.toDomain(): Property<*> =
  PropertyConfiguration.find(name = id.name).let { propertyConfiguration ->
    when (propertyConfiguration?.type) {
      STRING -> StringProp(propertyConfiguration, id.projectId, rawValue, createdAt, updatedAt)
      INTEGER -> IntegerProp(propertyConfiguration, id.projectId, rawValue, createdAt, updatedAt)
      DECIMAL -> DecimalProp(propertyConfiguration, id.projectId, rawValue, createdAt, updatedAt)
      FLAG -> FlagProp(propertyConfiguration, id.projectId, rawValue, createdAt, updatedAt)
      null -> throw IllegalArgumentException("Couldn't resolve property type of $id")
    }
  }

fun Property<*>.toData(
  project: Project,
): PropertyDbm = PropertyDbm(
  id = PropertyIdDbm(name = config.name, projectId = project.id),
  project = project.toData(),
  rawValue = rawValue,
  createdAt = createdAt,
  updatedAt = updatedAt,
)
