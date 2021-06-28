package com.appifyhub.monolith.repository.admin

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.mapper.withCast
import com.appifyhub.monolith.storage.dao.PropertyDao
import com.appifyhub.monolith.storage.model.admin.PropertyDbm
import com.appifyhub.monolith.storage.model.admin.PropertyIdDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PropertyRepositoryImpl(
  private val propertyDao: PropertyDao,
  private val timeProvider: TimeProvider,
) : PropertyRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun <T : Any> fetchProperty(project: Project, config: PropertyConfiguration): Property<T> {
    log.debug("Fetching property ${config.name} from $project")
    return propertyDao.findById(PropertyIdDbm(config.name, project.id)).get().toDomain().withCast()
  }

  override fun fetchProperties(project: Project, configs: List<PropertyConfiguration>): List<Property<*>> {
    log.debug("Fetching properties {${configs.joinToString { it.name }}} from $project")
    return propertyDao.findAllByIdIn(configs.map { PropertyIdDbm(it.name, project.id) }).map(PropertyDbm::toDomain)
  }

  override fun fetchAllProperties(project: Project): List<Property<*>> {
    log.debug("Fetching all properties from $project")
    return propertyDao.findAllById_ProjectId(project.id).map(PropertyDbm::toDomain)
  }

  override fun <T : Any> saveProperty(project: Project, property: Property<T>): Property<T> {
    log.debug("Saving property $property")
    val propertyData = property.toData(project).apply {
      updatedAt = timeProvider.currentDate
    }
    return propertyDao.save(propertyData).toDomain().withCast()
  }

  override fun saveProperties(project: Project, properties: List<Property<*>>): List<Property<*>> {
    log.debug("Saving properties ${properties.joinToString()}")
    val now = timeProvider.currentDate
    val propertiesData = properties.map {
      it.toData(project).apply { updatedAt = now }
    }
    return propertyDao.saveAll(propertiesData).map(PropertyDbm::toDomain)
  }

  override fun <T : Any> clearProperty(property: Property<T>) {
    log.debug("Clearing property $property")
    return propertyDao.deleteById(PropertyIdDbm(property.config.name, property.projectId))
  }

  override fun clearProperty(project: Project, config: PropertyConfiguration) {
    log.debug("Clearing property ${config.name} from $project")
    return propertyDao.deleteById(PropertyIdDbm(config.name, project.id))
  }

  override fun clearProperties(properties: List<Property<*>>) {
    log.debug("Clearing properties ${properties.joinToString()}")
    return propertyDao.deleteAllByIdIn(properties.map { PropertyIdDbm(it.config.name, it.projectId) })
  }

  override fun clearProperties(project: Project, configs: List<PropertyConfiguration>) {
    log.debug("Clearing properties ${configs.joinToString { it.name }} from $project")
    return propertyDao.deleteAllByIdIn(configs.map { PropertyIdDbm(it.name, project.id) })
  }

  override fun clearAllProperties(project: Project) {
    log.debug("Clearing all properties from $project")
    return propertyDao.deleteAllById_ProjectId(project.id)
  }

}
