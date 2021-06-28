package com.appifyhub.monolith.service.admin

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.property.ops.PropertyFilter
import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration
import com.appifyhub.monolith.domain.mapper.instantiate
import com.appifyhub.monolith.domain.mapper.withCast
import com.appifyhub.monolith.network.admin.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.mapper.toDomain
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.admin.PropertyRepository
import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.throwNormalization
import com.appifyhub.monolith.util.ext.throwPropertyNotFound
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PropertyServiceImpl(
  private val repository: PropertyRepository,
  private val adminRepository: AdminRepository,
  private val timeProvider: TimeProvider,
) : PropertyService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun getConfigurationsFiltered(params: PropertyFilterQueryParams?): List<PropertyConfiguration> {
    log.debug("Getting property configurations for $params")
    val filter = requireFilter(params)
    return PropertyConfiguration.filter(filter)
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

    return PropertyConfiguration.filter(filter)
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

    repository.clearProperty(project, config)
  }

  override fun clearPropertiesFiltered(projectId: Long, params: PropertyFilterQueryParams?) {
    log.debug("Clearing properties $params for project $projectId")

    val project = requireProject(projectId)
    val filter = requireFilter(params)

    PropertyConfiguration.filter(filter)
      .takeIf { it.isNotEmpty() }
      ?.let { repository.clearProperties(project, it) }
  }

  // Helpers

  @Throws private fun requireProject(projectId: Long): Project {
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    return adminRepository.fetchProjectById(normalizedProjectId)
  }

  @Throws private fun requireConfig(propName: String) =
    PropertyConfiguration.find(propName) ?: throwPropertyNotFound(propName)

  @Throws private fun requireFilter(queryParams: PropertyFilterQueryParams?): PropertyFilter? = try {
    queryParams?.toDomain()
  } catch (t: Throwable) {
    log.warn("Failed to normalize filter", t)
    throwNormalization { "Property filter is invalid" }
  }

}
