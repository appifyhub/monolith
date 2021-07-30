package com.appifyhub.monolith.service.admin

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.ProjectCreationInfo
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.silent
import com.appifyhub.monolith.util.ext.throwLocked
import com.appifyhub.monolith.util.ext.throwNotFound
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AdminServiceImpl(
  private val adminRepository: AdminRepository,
) : AdminService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addProject(creationInfo: ProjectCreationInfo, creator: User?): Project {
    log.debug("Adding project $creationInfo with creator $creator")

    val adminProject = silent { getAdminProject() }
    if (adminProject != null) {
      // admin project is already created
      if (creator == null) error("Project creator must be provided")

      if (creator.id.projectId != adminProject.id)
        throwLocked { "Projects can be added only by admin project users" }
    } else {
      // looks like we're creating the admin project
      if (creator != null) error("Project creator must not be provided for admin project")
    }

    return adminRepository.addProject(creationInfo, creator)
  }

  override fun getAdminProject(): Project {
    log.debug("Getting admin project")
    return adminRepository.getAdminProject()
  }

  override fun getAdminOwner(): User {
    log.debug("Getting admin owner")
    return adminRepository.getAdminOwner()
  }

  override fun fetchProjectById(id: Long): Project {
    log.debug("Fetching project by id $id")
    val normalizedProjectId = Normalizers.ProjectId.run(id).requireValid { "Project ID" }
    return adminRepository.fetchProjectById(normalizedProjectId)
  }

  override fun fetchAllProjectsByCreator(creator: User): List<Project> {
    log.debug("Fetching all projects for creator $creator")

    if (creator.id.projectId != getAdminProject().id)
      throwNotFound { "Non-service creators don't have any projects" }

    return adminRepository.fetchAllProjectsByCreatorUserId(creator.id)
  }

  override fun fetchProjectCreator(projectId: Long): User {
    log.debug("Fetching project creator for $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    return adminRepository.fetchProjectCreator(normalizedProjectId)
  }

  override fun updateProject(updater: ProjectUpdater): Project {
    log.debug("Updating project $updater")

    val normalizedProjectId = Normalizers.ProjectId.run(updater.id).requireValid { "Project ID" }

    val normalizedUpdater = ProjectUpdater(
      id = normalizedProjectId,
      type = updater.type,
      status = updater.status,
    )

    return adminRepository.updateProject(normalizedUpdater)
  }

  override fun removeProjectById(projectId: Long) {
    log.debug("Removing project $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    // we can't delete the admin project
    if (getAdminProject().id == projectId)
      throwLocked { "Admin project can't be deleted" }

    return adminRepository.removeProjectById(normalizedProjectId)
  }

  override fun removeAllProjectsByCreator(creator: User) {
    log.debug("Removing all projects for creator $creator")

    // we can't delete the admin project
    if (creator.id.projectId != getAdminProject().id)
      throwLocked { "Projects can be removed only by admin project users" }

    adminRepository.removeAllProjectsByCreator(creator.id)
  }

}
