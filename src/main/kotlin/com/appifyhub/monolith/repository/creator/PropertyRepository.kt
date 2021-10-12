package com.appifyhub.monolith.repository.creator

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.ProjectProperty

interface PropertyRepository {

  @Throws fun <T : Any> fetchProperty(project: Project, config: ProjectProperty): Property<T>

  @Throws fun fetchProperties(project: Project, configs: List<ProjectProperty>): List<Property<*>>

  @Throws fun fetchAllProperties(project: Project): List<Property<*>>

  @Throws fun <T : Any> saveProperty(project: Project, property: Property<T>): Property<T>

  @Throws fun saveProperties(project: Project, properties: List<Property<*>>): List<Property<*>>

  @Throws fun <T : Any> clearProperty(property: Property<T>)

  @Throws fun clearProperty(project: Project, config: ProjectProperty)

  @Throws fun clearProperties(properties: List<Property<*>>)

  @Throws fun clearProperties(project: Project, configs: List<ProjectProperty>)

  @Throws fun clearAllProperties(project: Project)

}
