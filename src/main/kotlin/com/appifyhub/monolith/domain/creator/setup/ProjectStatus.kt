package com.appifyhub.monolith.domain.creator.setup

import com.appifyhub.monolith.domain.creator.Project.Status
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.service.access.AccessManager.Feature

data class ProjectStatus(
  val status: Status,
  val usableFeatures: List<Feature>,
  val unusableFeatures: List<Feature>,
  val properties: List<Property<*>>,
)
