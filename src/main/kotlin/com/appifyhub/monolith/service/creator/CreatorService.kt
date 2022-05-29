package com.appifyhub.monolith.service.creator

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId

interface CreatorService {

  companion object {
    const val DEFAULT_MAX_USERS = 1000
  }

  @Throws fun addProject(projectData: ProjectCreator): Project

  @Throws fun getCreatorProject(): Project

  @Throws fun getSuperCreator(): User

  @Throws fun fetchAllProjects(): List<Project>

  @Throws fun fetchProjectById(id: Long): Project

  @Throws fun fetchAllProjectsByCreator(creator: User): List<Project>

  @Throws fun fetchProjectCreator(projectId: Long): User

  @Throws fun updateProject(updater: ProjectUpdater): Project

  @Throws fun removeProjectById(projectId: Long)

  @Throws fun removeAllProjectsByCreator(creatorId: UserId)

}
