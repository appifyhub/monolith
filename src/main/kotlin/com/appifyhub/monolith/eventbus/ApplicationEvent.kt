package com.appifyhub.monolith.eventbus

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.user.User

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
