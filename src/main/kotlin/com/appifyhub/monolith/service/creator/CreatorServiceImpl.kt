package com.appifyhub.monolith.service.creator

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreator
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.creator.CreatorRepository
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.silent
import com.appifyhub.monolith.util.ext.throwLocked
import com.appifyhub.monolith.util.ext.throwNotFound
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CreatorServiceImpl(
  private val creatorRepository: CreatorRepository,
) : CreatorService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addProject(projectInfo: ProjectCreator): Project {
    log.debug("Adding project $projectInfo")

    val creatorProject = silent(log = false) { getCreatorProject() }
    if (creatorProject != null) {
      // creator project is already created
      if (projectInfo.owner == null) error("Project creator must be provided")

      if (projectInfo.owner.id.projectId != creatorProject.id)
        throwLocked { "Projects can be added only by creator project users" }
    } else {
      // looks like we're creating the creator project
      if (projectInfo.owner != null) error("Project's future owner must not be provided for creator project")
    }

    return creatorRepository.addProject(projectInfo)
  }

  override fun fetchAllProjects(): List<Project> {
    log.debug("Getting all creator projects")
    return creatorRepository.fetchAllProjects()
  }

  override fun getCreatorProject(): Project {
    log.debug("Getting creator project")
    return creatorRepository.getCreatorProject()
  }

  override fun getSuperCreator(): User {
    log.debug("Getting creator owner")
    return creatorRepository.getSuperCreator()
  }

  override fun fetchProjectById(id: Long): Project {
    log.debug("Fetching project by id $id")
    val normalizedProjectId = Normalizers.ProjectId.run(id).requireValid { "Project ID" }
    return creatorRepository.fetchProjectById(normalizedProjectId)
  }

  override fun fetchAllProjectsByCreator(creator: User): List<Project> {
    log.debug("Fetching all projects for creator $creator")

    if (creator.id.projectId != getCreatorProject().id)
      throwNotFound { "Non-service creators don't have any projects" }

    return creatorRepository.fetchAllProjectsByCreatorUserId(creator.id)
  }

  override fun fetchProjectCreator(projectId: Long): User {
    log.debug("Fetching project creator for $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    return creatorRepository.fetchProjectCreator(normalizedProjectId)
  }

  override fun updateProject(updater: ProjectUpdater): Project {
    log.debug("Updating project $updater")

    val normalizedProjectId = Normalizers.ProjectId.run(updater.id).requireValid { "Project ID" }
    val normalizedLanguageTag = Normalizers.LanguageTag.run(updater.languageTag?.value).requireValid { "Language Tag" }

    val normalizedUpdater = ProjectUpdater(
      id = normalizedProjectId,
      type = updater.type,
      status = updater.status,
      languageTag = Settable(normalizedLanguageTag),
    )

    return creatorRepository.updateProject(normalizedUpdater)
  }

  override fun removeProjectById(projectId: Long) {
    log.debug("Removing project $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    // we can't delete the creator project
    if (getCreatorProject().id == projectId)
      throwLocked { "Creator project can't be deleted" }

    return creatorRepository.removeProjectById(normalizedProjectId)
  }

  override fun removeAllProjectsByCreator(creatorId: UserId) {
    log.debug("Removing all projects for creator $creatorId")

    // we can't delete the creator project
    if (creatorId.projectId != getCreatorProject().id)
      throwLocked { "Projects can be removed only by creator project users" }

    creatorRepository.removeAllProjectsByCreator(creatorId)
  }

}
