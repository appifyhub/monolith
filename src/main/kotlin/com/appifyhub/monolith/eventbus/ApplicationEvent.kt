package com.appifyhub.monolith.eventbus

import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.user.domain.model.User

sealed interface ApplicationEvent<out P> {
  val ownerProject: Project
  val payload: P
}

data class ProjectCreated(
  override val ownerProject: Project,
  override val payload: Project,
) : ApplicationEvent<Project>

data class UserCreated(
  override val ownerProject: Project,
  override val payload: User,
) : ApplicationEvent<User>

data class UserAuthResetCompleted(
  override val ownerProject: Project,
  override val payload: User,
) : ApplicationEvent<User>
