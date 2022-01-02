package com.appifyhub.monolith.repository.creator

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId

interface CreatorRepository {

  @Throws fun addProject(projectInfo: ProjectCreator): Project

  @Throws fun getCreatorProject(): Project

  @Throws fun getSuperCreator(): User

  @Throws fun fetchAllProjects(): List<Project>

  @Throws fun fetchProjectById(id: Long): Project

  @Throws fun fetchAllProjectsByCreatorUserId(id: UserId): List<Project>

  @Throws fun fetchProjectCreator(projectId: Long): User

  @Throws fun updateProject(updater: ProjectUpdater): Project

  @Throws fun removeProjectById(projectId: Long)

  @Throws fun removeAllProjectsByCreator(creatorId: UserId)

}
