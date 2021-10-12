package com.appifyhub.monolith.repository.creator

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.ops.ProjectCreationInfo
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.mapper.applyTo
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.mapper.toProjectData
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.storage.dao.ProjectCreationDao
import com.appifyhub.monolith.storage.dao.ProjectDao
import com.appifyhub.monolith.storage.model.creator.ProjectCreationDbm
import com.appifyhub.monolith.storage.model.creator.ProjectCreationKeyDbm
import com.appifyhub.monolith.storage.model.creator.ProjectDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class CreatorRepositoryImpl(
  private val projectDao: ProjectDao,
  private val creationDao: ProjectCreationDao,
  private val userRepository: UserRepository,
  private val timeProvider: TimeProvider,
) : CreatorRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  private val lazyCreatorProject: Project by lazy {
    projectDao.findAll()
      .map(ProjectDbm::toDomain)
      .minByOrNull { it.id }!!
  }

  private val lazyCreatorOwner: User by lazy {
    userRepository.fetchAllUsersByProjectId(lazyCreatorProject.id)
      .filter { it.authority == Authority.OWNER }
      .minByOrNull { it.createdAt.time }!!
  }

  override fun addProject(creationInfo: ProjectCreationInfo, creator: User?): Project {
    log.debug("Adding project $creationInfo with creator $creator")

    // store the project itself
    val project = projectDao.save(
      creationInfo.toProjectData(timeProvider = timeProvider)
    ).toDomain()

    creator?.let {
      // store creation record for ownership mapping
      creationDao.save(
        ProjectCreationDbm(
          data = ProjectCreationKeyDbm(
            creatorUserId = it.id.userId,
            creatorProjectId = it.id.projectId,
            createdProjectId = project.id,
          ),
          user = it.toData(),
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

  override fun getCreatorOwner(): User {
    log.debug("Getting creator owner")
    return lazyCreatorOwner
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

    // cascade manually for now
    userRepository.removeAllUsersByProjectId(projectId)
    creationDao.deleteAllByData_CreatedProjectId(projectId)

    // remove the project itself
    projectDao.deleteById(projectId)
  }

  override fun removeAllProjectsByCreator(creatorUserId: UserId) {
    log.debug("Removing all projects for creator $creatorUserId")

    // find the projects marked for deletion
    val projects = creationDao.findAllByData_CreatorUserIdAndData_CreatorProjectId(
      userId = creatorUserId.userId,
      projectId = creatorUserId.projectId,
    ).map { it.project.toDomain() }

    // cascade manually for now
    projects.forEach { userRepository.removeAllUsersByProjectId(it.id) }
    creationDao.deleteAllByData_CreatorUserIdAndData_CreatorProjectId(creatorUserId.userId, creatorUserId.projectId)

    // remove the projects
    projectDao.deleteAll(projects.map(Project::toData))
  }

}
