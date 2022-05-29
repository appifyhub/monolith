package com.appifyhub.monolith.controller.creator

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.network.creator.project.ProjectResponse
import com.appifyhub.monolith.network.creator.project.ops.ProjectCreateRequest
import com.appifyhub.monolith.network.creator.project.ops.ProjectUpdateRequest
import com.appifyhub.monolith.network.mapper.toDomain
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import com.appifyhub.monolith.service.creator.CreatorService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CreatorProjectController(
  private val creatorService: CreatorService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.PROJECTS)
  fun addProject(
    authentication: Authentication,
    @RequestBody projectRequest: ProjectCreateRequest,
  ): ProjectResponse {
    log.debug("[POST] create a new project with $projectRequest")

    val ownerId = UserId.fromUniversalFormat(projectRequest.ownerUniversalId)
    val requester = accessManager.requestCreator(authentication, matchesId = ownerId, requireVerified = true)
    val projectData = projectRequest.toDomain(owner = requester)
    val project = creatorService.addProject(projectData)
    val status = accessManager.fetchProjectState(project.id)

    return project.toNetwork(status)
  }

  @GetMapping(Endpoints.PROJECTS)
  fun getProjects(
    authentication: Authentication,
    @RequestParam("creator_id", required = false) universalCreatorId: String? = null,
  ): List<ProjectResponse> {
    log.debug("[GET] get creator projects, universalCreatorId = $universalCreatorId")

    val creatorId = universalCreatorId?.let { UserId.fromUniversalFormat(it) }
    val creator = accessManager.requestCreator(authentication, matchesId = creatorId, requireVerified = true)

    val projects = if (universalCreatorId != null) {
      creatorService.fetchAllProjectsByCreator(creator)
    } else {
      creatorService.fetchAllProjects()
    }

    return projects.map {
      it.toNetwork(
        projectState = accessManager.fetchProjectState(it.id),
      )
    }
  }

  @GetMapping(Endpoints.PROJECT)
  fun getProject(
    authentication: Authentication,
    @PathVariable projectId: Long,
  ): ProjectResponse {
    log.debug("[GET] get creator project $projectId")

    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    val status = accessManager.fetchProjectState(projectId)

    return project.toNetwork(projectState = status)
  }

  @PutMapping(Endpoints.PROJECT)
  fun updateProject(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestBody updateRequest: ProjectUpdateRequest,
  ): ProjectResponse {
    log.debug("[PUT] update creator project $projectId with request $updateRequest")

    var projectUpdater = updateRequest.toDomain(projectId)

    // verify access rights for editing this project
    accessManager.requestProjectAccess(
      authData = authentication,
      targetId = projectId,
      privilege = Privilege.PROJECT_WRITE,
    )

    // only super-creator can change the project status
    if (updateRequest.status != null)
      accessManager.requestSuperCreator(authentication)

    // force REVIEW state if type is changed
    if (updateRequest.type != null && updateRequest.status == null) {
      projectUpdater = projectUpdater.copy(status = Settable(Project.Status.REVIEW))
    }

    val project = creatorService.updateProject(projectUpdater)
    val status = accessManager.fetchProjectState(projectId)

    return project.toNetwork(projectState = status)
  }

  @DeleteMapping(Endpoints.PROJECT)
  fun removeProject(
    authentication: Authentication,
    @PathVariable projectId: Long,
  ): SimpleResponse {
    log.debug("[DELETE] remove creator project $projectId")

    accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_WRITE)
    creatorService.removeProjectById(projectId)

    return SimpleResponse.DONE
  }

  @DeleteMapping(Endpoints.PROJECTS)
  fun removeProjectsByCreator(
    authentication: Authentication,
    @RequestParam("creator_id") universalCreatorId: String,
  ): SimpleResponse {
    log.debug("[DELETE] remove all creator projects for $universalCreatorId")

    val creatorId = UserId.fromUniversalFormat(universalCreatorId)
    accessManager.requestCreator(authentication, matchesId = creatorId, requireVerified = true)

    creatorService.removeAllProjectsByCreator(creatorId)

    return SimpleResponse.DONE
  }

}
