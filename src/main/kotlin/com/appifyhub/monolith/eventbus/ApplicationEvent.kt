package com.appifyhub.monolith.eventbus

import com.appifyhub.monolith.domain.creator.Project

sealed interface ApplicationEvent<out P> {
  val ownerProject: Project
  val payload: P
}

data class ProjectCreated(
  override val ownerProject: Project,
  override val payload: Project,
) : ApplicationEvent<Project>
