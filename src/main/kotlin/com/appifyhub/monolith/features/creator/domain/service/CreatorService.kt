package com.appifyhub.monolith.features.creator.domain.service

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectCreator
import com.appifyhub.monolith.features.creator.domain.model.ProjectUpdater

interface CreatorService {

  companion object {
    // should be service config probably… but yeah, life's short
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
