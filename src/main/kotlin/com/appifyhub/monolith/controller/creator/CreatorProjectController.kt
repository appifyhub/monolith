package com.appifyhub.monolith.controller.creator

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.network.creator.ProjectResponse
import com.appifyhub.monolith.network.creator.ops.ProjectCreateRequest
import com.appifyhub.monolith.network.mapper.toDomain
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import com.appifyhub.monolith.service.creator.CreatorService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
    val status = accessManager.fetchProjectStatus(project.id)

    return project.toNetwork(status)
  }

  @GetMapping(Endpoints.PROJECTS)
  fun getProjects(
    authentication: Authentication,
    @RequestParam(required = false) universalCreatorId: String? = null,
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
        projectStatus = accessManager.fetchProjectStatus(it.id),
      )
    }
  }

  @GetMapping(Endpoints.ANY_PROJECT)
  fun getProject(
    authentication: Authentication,
    @PathVariable projectId: Long,
  ): ProjectResponse {
    log.debug("[GET] get creator project")

    val project = accessManager.requestProjectAccess(authentication, projectId, Privilege.PROJECT_READ)
    val status = accessManager.fetchProjectStatus(projectId)

    return project.toNetwork(projectStatus = status)
  }

}
