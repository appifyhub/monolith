package com.appifyhub.monolith.controller.creator

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.creator.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.creator.property.PropertyResponse
import com.appifyhub.monolith.network.creator.property.ops.MultiplePropertiesSaveRequest
import com.appifyhub.monolith.network.creator.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.creator.property.ops.PropertySaveRequest
import com.appifyhub.monolith.network.creator.property.ops.PropertyValueDto
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import com.appifyhub.monolith.service.creator.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CreatorPropertyController(
  private val propertyService: PropertyService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(Endpoints.PROJECT_CONFIGS)
  fun getConfigurations(
    authentication: Authentication,
    @PathVariable projectId: Long,
    query: PropertyFilterQueryParams?,
  ): List<PropertyConfigurationResponse> {
    log.debug("[GET] get configurations for project $projectId with query $query")

    accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    val configurations = propertyService.getConfigurationsFiltered(query)

    return configurations.map(ProjectProperty::toNetwork)
  }

  @GetMapping(Endpoints.PROJECT_PROPERTY)
  fun getProperty(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable propertyName: String,
  ): PropertyResponse {
    log.debug("[GET] get property $propertyName for project $projectId")

    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    val property = propertyService.fetchProperty<Any>(project.id, propertyName)

    return property.toNetwork()
  }

  @GetMapping(Endpoints.PROJECT_PROPERTIES)
  fun getProperties(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestParam names: List<String>?,
    query: PropertyFilterQueryParams?,
  ): List<PropertyResponse> {
    log.debug("[GET] get properties for project $projectId with query $query and names $names")

    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    val properties = if (names.isNullOrEmpty()) {
      propertyService.fetchPropertiesFiltered(project.id, query)
    } else {
      propertyService.fetchProperties(project.id, names)
    }

    return properties.map(Property<*>::toNetwork)
  }

  @PostMapping(Endpoints.PROJECT_PROPERTY)
  fun saveProperty(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable propertyName: String,
    @RequestBody propertyRequest: PropertySaveRequest,
  ): PropertyResponse {
    log.debug("[GET] save property $propertyName for project $projectId")

    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    val property = propertyService.saveProperty<Any>(project.id, propertyName, propertyRequest.rawValue)

    return property.toNetwork()
  }

  @PostMapping(Endpoints.PROJECT_PROPERTIES)
  fun saveProperties(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestBody propertiesRequest: MultiplePropertiesSaveRequest,
  ): List<PropertyResponse> {
    log.debug("[GET] save properties $propertiesRequest for project $projectId")

    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    val names = propertiesRequest.properties.map(PropertyValueDto::name)
    val values = propertiesRequest.properties.map(PropertyValueDto::rawValue)
    val properties = propertyService.saveProperties(project.id, names, values)

    return properties.map(Property<*>::toNetwork)
  }

  @DeleteMapping(Endpoints.PROJECT_PROPERTY)
  fun clearProperties(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable propertyName: String,
  ): MessageResponse {
    log.debug("[GET] clear property $propertyName for project $projectId")

    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    propertyService.clearProperty(project.id, propertyName)

    return MessageResponse.DONE
  }

  @DeleteMapping(Endpoints.PROJECT_PROPERTIES)
  fun clearProperties(
    authentication: Authentication,
    @PathVariable projectId: Long,
    query: PropertyFilterQueryParams?,
  ): MessageResponse {
    log.debug("[GET] clear properties for project $projectId with query $query")

    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    propertyService.clearPropertiesFiltered(project.id, query)

    return MessageResponse.DONE
  }

}