package com.appifyhub.monolith.domain.creator.setup

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature

data class ProjectState(
  val project: Project,
  val usableFeatures: List<Feature>,
  val unusableFeatures: List<Feature>,
)
