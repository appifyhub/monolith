package com.appifyhub.monolith.service.admin

import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.domain.admin.property.ProjectProperty
import com.appifyhub.monolith.network.admin.property.ops.PropertyFilterQueryParams

interface PropertyService {

  @Throws fun getConfigurationsFiltered(params: PropertyFilterQueryParams?): List<ProjectProperty>

  @Throws fun <T : Any> fetchProperty(projectId: Long, propName: String): Property<T>

  @Throws fun fetchProperties(projectId: Long, propNames: List<String>): List<Property<*>>

  @Throws fun fetchPropertiesFiltered(projectId: Long, params: PropertyFilterQueryParams?): List<Property<*>>

  @Throws fun <T : Any> saveProperty(projectId: Long, propName: String, propRawValue: String): Property<T>

  @Throws fun saveProperties(projectId: Long, propNames: List<String>, propRawValues: List<String>): List<Property<*>>

  @Throws fun clearProperty(projectId: Long, propName: String)

  @Throws fun clearPropertiesFiltered(projectId: Long, params: PropertyFilterQueryParams?)

}
