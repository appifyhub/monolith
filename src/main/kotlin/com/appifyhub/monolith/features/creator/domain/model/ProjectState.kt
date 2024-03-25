package com.appifyhub.monolith.features.creator.domain.model

import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature

data class ProjectState(
  val project: Project,
  val usableFeatures: List<Feature>,
  val unusableFeatures: List<Feature>,
)
