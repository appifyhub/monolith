package com.appifyhub.monolith.domain.mapper

import com.appifyhub.monolith.domain.common.applySettable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.Property.DecimalProp
import com.appifyhub.monolith.domain.creator.property.Property.FlagProp
import com.appifyhub.monolith.domain.creator.property.Property.IntegerProp
import com.appifyhub.monolith.domain.creator.property.Property.StringProp
import com.appifyhub.monolith.domain.creator.property.PropertyType.DECIMAL
import com.appifyhub.monolith.domain.creator.property.PropertyType.FLAG
import com.appifyhub.monolith.domain.creator.property.PropertyType.INTEGER
import com.appifyhub.monolith.domain.creator.property.PropertyType.STRING
import com.appifyhub.monolith.storage.model.creator.ProjectDbm
import com.appifyhub.monolith.storage.model.creator.PropertyDbm
import com.appifyhub.monolith.storage.model.creator.PropertyIdDbm
import com.appifyhub.monolith.util.TimeProvider
import java.util.Date

fun ProjectUpdater.applyTo(
  project: Project,
  timeProvider: TimeProvider,
): Project = project
  .applySettable(type) { copy(type = it) }
  .applySettable(status) { copy(status = it) }
  .applySettable(languageTag) { copy(languageTag = it) }
  .copy(updatedAt = timeProvider.currentDate)

fun ProjectCreator.toProjectData(
  timeProvider: TimeProvider,
): ProjectDbm = ProjectDbm(
  projectId = null,
  type = type.name,
  status = status.name,
  userIdType = userIdType.name,
  languageTag = languageTag,
  createdAt = timeProvider.currentDate,
  updatedAt = timeProvider.currentDate,
)

fun ProjectDbm.toDomain(): Project = Project(
  id = projectId!!,
  type = Project.Type.find(type, default = Project.Type.COMMERCIAL),
  status = Project.Status.find(status, default = Project.Status.REVIEW),
  userIdType = Project.UserIdType.find(userIdType, default = Project.UserIdType.RANDOM),
  languageTag = languageTag,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Project.toData(): ProjectDbm = ProjectDbm(
  projectId = id,
  type = type.name,
  status = status.name,
  userIdType = userIdType.name,
  languageTag = languageTag,
  createdAt = createdAt,
  updatedAt = updatedAt,
)

fun Property.Companion.instantiate(
  config: ProjectProperty,
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
  config = ProjectProperty.findOrNull(name = id.name)!!,
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
