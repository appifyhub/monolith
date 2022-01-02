package com.appifyhub.monolith.network.mapper

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.network.creator.project.ops.ProjectCreateRequest
import com.appifyhub.monolith.network.creator.project.ops.ProjectUpdateRequest

fun ProjectCreateRequest.toDomain(
  owner: User? = null,
): ProjectCreator = ProjectCreator(
  owner = owner,
  type = Project.Type.find(type, Project.Type.COMMERCIAL),
  status = Project.Status.REVIEW,
  userIdType = Project.UserIdType.find(userIdType, Project.UserIdType.RANDOM),
)

fun ProjectUpdateRequest.toDomain(
  projectId: Long,
): ProjectUpdater = ProjectUpdater(
  id = projectId,
  type = type.mapToDomainNonNull { Project.Type.find(it) },
  status = status.mapToDomainNonNull { Project.Status.find(it) },
)
