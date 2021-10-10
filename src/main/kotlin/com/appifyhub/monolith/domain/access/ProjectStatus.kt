package com.appifyhub.monolith.domain.access

import com.appifyhub.monolith.domain.admin.Project.Status
import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.service.access.AccessManager.Feature

data class ProjectStatus(
  val status: Status,
  val usableFeatures: List<Feature>,
  val unusableFeatures: List<Feature>,
  val properties: List<Property<*>>,
)
