package com.appifyhub.monolith.controller.creator

import com.appifyhub.monolith.network.creator.ProjectResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.service.access.AccessManager
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import com.appifyhub.monolith.service.creator.CreatorService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CreatorProjectController(
  private val creatorService: CreatorService,
  private val accessManager: AccessManager,
) {

  object Endpoints {
    const val PROJECTS = "/v1/projects"
    const val ANY_PROJECT = "/v1/projects/{projectId}"
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  @GetMapping(Endpoints.PROJECTS)
  fun getAllProjects(
    authentication: Authentication,
  ): List<ProjectResponse> {
    log.debug("[GET] get all creator projects")

    accessManager.requestSuperCreator(authentication)

    val projects = creatorService.fetchAllProjects()
    return projects.map { project ->
      val status = accessManager.fetchProjectStatus(project.id)
      project.toNetwork(status)
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
