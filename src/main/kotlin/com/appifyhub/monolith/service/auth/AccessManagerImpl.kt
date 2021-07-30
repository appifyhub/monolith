package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.auth.AccessManager.Privilege
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.silent
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class AccessManagerImpl(
  private val authService: AuthService,
  private val userService: UserService,
  private val adminService: AdminService,
) : AccessManager {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun requestUserAccess(authData: Authentication, targetId: UserId, privilege: Privilege): User {
    log.debug("Authentication $authData requesting '${privilege.name}' access to $targetId")

    // validate request data and token
    val normalizedTargetId = Normalizers.UserId.run(targetId).requireValid { "User ID" }
    val jwt = authService.requireValidJwt(authData, shallow = false)
    val tokenDetails = authService.fetchTokenDetails(jwt)

    // allow request if it's the project creator requesting
    val isRequesterProjectCreator = fetchCreator(normalizedTargetId.projectId)?.id == tokenDetails.ownerId
    val isRequesterSuperOwner = getAdminOwner().id == tokenDetails.ownerId
    if (isRequesterProjectCreator || isRequesterSuperOwner) return fetchUser(normalizedTargetId)

    // not project creator; validate that the project matches
    val targetProject = fetchProject(normalizedTargetId.projectId)
    val isSameProjectRequest = targetProject.id == tokenDetails.projectId
    require(isSameProjectRequest) { "Only requests within the same project are allowed" }

    // self access is always allowed, check if that's the case
    val requesterUser = fetchUser(tokenDetails.ownerId)
    if (requesterUser.id == normalizedTargetId) return requesterUser

    // static tokens are always allowed (unless it's creators looking for other creators)
    val isCreatorRequest = getAdminProject().id == requesterUser.id.projectId
    val isCrossCreatorAccess = !isRequesterSuperOwner && isCreatorRequest
    if (tokenDetails.isStatic && !isCrossCreatorAccess) return fetchUser(normalizedTargetId)

    // check if minimum authorization level is met
    val isPrivileged = requesterUser.canActAs(privilege.level)
    require(isPrivileged) { "Only ${privilege.level.groupName} are authorized" }

    // check if authorization level is enough (mods can't access other mods)
    val targetUser = fetchUser(normalizedTargetId)
    val isHigherAuthority = requesterUser.authority.ordinal > targetUser.authority.ordinal
    require(isHigherAuthority) { "Only ${targetUser.authority.nextGroupName} are authorized" }

    return fetchUser(normalizedTargetId)
  }

  override fun requestProjectAccess(authData: Authentication, targetId: Long, privilege: Privilege): Project {
    log.debug("Authentication $authData requesting '${privilege.name}' access to $targetId")

    // validate request data and token
    val normalizedTargetId = Normalizers.ProjectId.run(targetId).requireValid { "Project ID" }
    val jwt = authService.requireValidJwt(authData, shallow = false)
    val tokenDetails = authService.fetchTokenDetails(jwt)

    // allow request if it's the project creator requesting
    val isRequesterProjectCreator = fetchCreator(normalizedTargetId)?.id == tokenDetails.ownerId
    val isRequesterSuperOwner = getAdminOwner().id == tokenDetails.ownerId
    if (isRequesterProjectCreator || isRequesterSuperOwner) return fetchProject(normalizedTargetId)

    // not project creator; validate that the project matches
    val isSameProjectRequest = tokenDetails.projectId == normalizedTargetId
    require(isSameProjectRequest) { "Only requests within the same project are allowed" }

    // static tokens are always allowed (unless it's creators looking for other creators)
    val requesterUser = fetchUser(tokenDetails.ownerId)
    val isCreatorRequest = getAdminProject().id == requesterUser.id.projectId
    val isCrossCreatorAccess = !isRequesterSuperOwner && isCreatorRequest
    if (tokenDetails.isStatic && !isCrossCreatorAccess) return fetchProject(normalizedTargetId)

    // check if minimum authorization level is met
    val isPrivileged = requesterUser.canActAs(privilege.level)
    require(isPrivileged) { "Only ${privilege.level.groupName} are authorized" }

    return fetchProject(normalizedTargetId)
  }

  // Helpers

  private fun getAdminProject() = adminService.getAdminProject()

  private fun getAdminOwner() = adminService.getAdminOwner()

  private fun fetchCreator(projectId: Long) = silent { adminService.fetchProjectCreator(projectId) }

  private fun fetchUser(id: UserId) = userService.fetchUserByUserId(id, withTokens = false)

  private fun fetchProject(id: Long) = adminService.fetchProjectById(id)

  private val TokenDetails.projectId
    get() = ownerId.projectId

}
