package com.appifyhub.monolith.repository.admin

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.ProjectCreationInfo
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId

interface AdminRepository {

  @Throws fun addProject(creationInfo: ProjectCreationInfo, creator: User?): Project

  @Throws fun getAdminProject(): Project

  @Throws fun getAdminOwner(): User

  @Throws fun fetchProjectById(id: Long): Project

  @Throws fun fetchAllProjectsByCreatorUserId(id: UserId): List<Project>

  @Throws fun fetchProjectCreator(projectId: Long): User

  @Throws fun updateProject(updater: ProjectUpdater): Project

  @Throws fun removeProjectById(projectId: Long)

  @Throws fun removeAllProjectsByCreator(creatorUserId: UserId)

}
