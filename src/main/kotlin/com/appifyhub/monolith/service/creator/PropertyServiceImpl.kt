package com.appifyhub.monolith.service.creator

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.ops.PropertyFilter
import com.appifyhub.monolith.domain.mapper.instantiate
import com.appifyhub.monolith.domain.mapper.withCast
import com.appifyhub.monolith.network.creator.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.mapper.toDomain
import com.appifyhub.monolith.repository.creator.CreatorRepository
import com.appifyhub.monolith.repository.creator.PropertyRepository
import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.throwLocked
import com.appifyhub.monolith.util.ext.throwNormalization
import com.appifyhub.monolith.util.ext.throwPropertyNotFound
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PropertyServiceImpl(
  private val repository: PropertyRepository,
  private val creatorRepository: CreatorRepository,
  private val timeProvider: TimeProvider,
) : PropertyService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun getConfigurationsFiltered(params: PropertyFilterQueryParams?): List<ProjectProperty> {
    log.debug("Getting property configurations for $params")
    val filter = requireFilter(params)
    return ProjectProperty.filter(filter)
  }

  override fun <T : Any> fetchProperty(projectId: Long, propName: String): Property<T> {
    log.debug("Fetching property $propName for project $projectId")

    val project = requireProject(projectId)
    val config = requireConfig(propName)

    return repository.fetchProperty(project, config)
  }

  override fun fetchProperties(projectId: Long, propNames: List<String>): List<Property<*>> {
    log.debug("Fetching properties ${propNames.joinToString()} for project $projectId")

    val project = requireProject(projectId)
    val configs = propNames.map(::requireConfig)

    return repository.fetchProperties(project, configs)
  }

  override fun fetchPropertiesFiltered(projectId: Long, params: PropertyFilterQueryParams?): List<Property<*>> {
    log.debug("Fetching properties $params for project $projectId")

    val project = requireProject(projectId)
    val filter = requireFilter(params)

    return ProjectProperty.filter(filter)
      .takeIf { it.isNotEmpty() }
      ?.let { repository.fetchProperties(project, it) }
      ?: emptyList()
  }

  override fun <T : Any> saveProperty(projectId: Long, propName: String, propRawValue: String): Property<T> {
    log.debug("Saving property $propName:$propRawValue for project $projectId")

    val project = requireProject(projectId)
    val config = requireConfig(propName)
    val normalizedRawValue = config.normalizer.run(propRawValue).requireValid { "Property ${config.name}" }
    val property = Property.instantiate(config, project.id, normalizedRawValue, timeProvider.currentDate)

    return repository.saveProperty(project, property).withCast()
  }

  override fun saveProperties(
    projectId: Long,
    propNames: List<String>,
    propRawValues: List<String>,
  ): List<Property<*>> {
    log.debug(
      "Saving properties ${propNames.joinToString()} " +
        "with values ${propRawValues.joinToString()} for project $projectId"
    )

    if (propNames.size != propRawValues.size)
      throwNormalization { "Property names need to correspond 1:1 with their values" }

    val project = requireProject(projectId)
    val configs = propNames.map(::requireConfig)
    val normalizedRawValues = propRawValues.mapIndexed { i, rawValue ->
      configs[i].normalizer.run(rawValue).requireValid { "Property ${configs[i].name}" }
    }
    val now = timeProvider.currentDate
    val properties = normalizedRawValues.mapIndexed { i, value ->
      Property.instantiate(configs[i], project.id, value, now)
    }

    return repository.saveProperties(project, properties)
  }

  override fun clearProperty(projectId: Long, propName: String) {
    log.debug("Clearing property $propName for project $projectId")

    val project = requireProject(projectId)
    val config = requireConfig(propName)

    if (config.isMandatory) throwLocked { "Property '${config.name}' is mandatory" }

    repository.clearProperty(project, config)
  }

  override fun clearPropertiesFiltered(projectId: Long, params: PropertyFilterQueryParams?) {
    log.debug("Clearing properties $params for project $projectId")

    val project = requireProject(projectId)
    val filter = requireFilter(params)?.copy(isMandatory = false)

    ProjectProperty.filter(filter)
      .takeIf { it.isNotEmpty() }
      ?.let { repository.clearProperties(project, it) }
  }

  // Helpers

  @Throws private fun requireProject(projectId: Long): Project {
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    return creatorRepository.fetchProjectById(normalizedProjectId)
  }

  @Throws private fun requireConfig(propName: String) =
    ProjectProperty.findOrNull(propName) ?: throwPropertyNotFound(propName)

  @Throws private fun requireFilter(queryParams: PropertyFilterQueryParams?): PropertyFilter? = try {
    queryParams?.toDomain()
  } catch (t: Throwable) {
    log.warn("Failed to normalize filter", t)
    throwNormalization { "Property filter is invalid" }
  }

}
