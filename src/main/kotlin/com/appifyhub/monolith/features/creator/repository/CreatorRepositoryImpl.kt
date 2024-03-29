package com.appifyhub.monolith.features.creator.repository

import com.appifyhub.monolith.features.user.domain.toData
import com.appifyhub.monolith.features.user.domain.toDomain
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.User.Authority
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.creator.domain.applyTo
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectCreator
import com.appifyhub.monolith.features.creator.domain.model.ProjectUpdater
import com.appifyhub.monolith.features.creator.domain.toData
import com.appifyhub.monolith.features.creator.domain.toDomain
import com.appifyhub.monolith.features.creator.domain.toProjectData
import com.appifyhub.monolith.features.creator.storage.ProjectCreationDao
import com.appifyhub.monolith.features.creator.storage.ProjectDao
import com.appifyhub.monolith.features.creator.storage.model.ProjectCreationDbm
import com.appifyhub.monolith.features.creator.storage.model.ProjectCreationKeyDbm
import com.appifyhub.monolith.features.creator.storage.model.ProjectDbm
import com.appifyhub.monolith.features.user.repository.UserRepository
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class CreatorRepositoryImpl(
  private val projectDao: ProjectDao,
  private val creationDao: ProjectCreationDao,
  private val userRepository: UserRepository,
  private val messageTemplateRepository: MessageTemplateRepository,
  private val timeProvider: TimeProvider,
) : CreatorRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  private val lazyCreatorProject: Project by lazy {
    projectDao.findAll()
      .map(ProjectDbm::toDomain)
      .minByOrNull { it.id }!!
  }

  private val lazySuperCreator: User by lazy {
    userRepository.fetchAllUsersByProjectId(lazyCreatorProject.id)
      .filter { it.authority == Authority.OWNER }
      .minByOrNull { it.createdAt.time }!!
  }

  override fun addProject(projectInfo: ProjectCreator): Project {
    log.debug("Adding project $projectInfo")

    // store the project itself
    val project = projectDao.save(
      projectInfo.toProjectData(timeProvider = timeProvider)
    ).toDomain()

    projectInfo.owner?.let { owner ->
      // store creation record for ownership mapping
      creationDao.save(
        ProjectCreationDbm(
          data = ProjectCreationKeyDbm(
            creatorUserId = owner.id.userId,
            creatorProjectId = owner.id.projectId,
            createdProjectId = project.id,
          ),
          user = owner.toData(),
          project = project.toData(),
        )
      )
    }

    return project
  }

  override fun getCreatorProject(): Project {
    log.debug("Getting creator project")
    return lazyCreatorProject
  }

  override fun getSuperCreator(): User {
    log.debug("Getting creator owner")
    return lazySuperCreator
  }

  override fun fetchAllProjects(): List<Project> {
    log.debug("Getting all creator projects")
    return projectDao.findAll().map(ProjectDbm::toDomain)
  }

  override fun fetchProjectById(id: Long): Project {
    log.debug("Fetching project by id $id")
    return projectDao.findById(id).get().toDomain()
  }

  override fun fetchAllProjectsByCreatorUserId(id: UserId): List<Project> {
    log.debug("Fetching all projects by creator $id")

    return creationDao.findAllByData_CreatorUserIdAndData_CreatorProjectId(id.userId, id.projectId)
      .map { it.data.createdProjectId }
      .let(projectDao::findAllById)
      .map(ProjectDbm::toDomain)
  }

  override fun fetchProjectCreator(projectId: Long): User {
    log.debug("Fetching project creator for project $projectId")

    return creationDao.findByData_CreatedProjectId(projectId).user.toDomain()
  }

  override fun updateProject(updater: ProjectUpdater): Project {
    log.debug("Updating project $updater")
    val fetchedProject = fetchProjectById(updater.id)
    val updatedProject = updater.applyTo(
      project = fetchedProject,
      timeProvider = timeProvider,
    )
    return projectDao.save(updatedProject.toData()).toDomain()
  }

  override fun removeProjectById(projectId: Long) {
    log.debug("Removing project $projectId")

    deleteCascadingDependants(projectId)
    creationDao.deleteAllByData_CreatedProjectId(projectId)

    projectDao.deleteById(projectId)
  }

  override fun removeAllProjectsByCreator(creatorId: UserId) {
    log.debug("Removing all projects for creator $creatorId")

    // find the projects marked for deletion
    val projects = creationDao.findAllByData_CreatorUserIdAndData_CreatorProjectId(
      userId = creatorId.userId,
      projectId = creatorId.projectId,
    ).map { it.project.toDomain() }

    projects.forEach { deleteCascadingDependants(it.id) }
    creationDao.deleteAllByData_CreatorUserIdAndData_CreatorProjectId(creatorId.userId, creatorId.projectId)

    projectDao.deleteAll(projects.map(Project::toData))
  }

  // Helpers

  private fun deleteCascadingDependants(projectId: Long) {
    // cascade manually for now
    userRepository.removeAllUsersByProjectId(projectId)
    messageTemplateRepository.deleteAllTemplatesByProjectId(projectId)
  }

}
