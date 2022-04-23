package com.appifyhub.monolith.service.access

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.setup.ProjectState
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.access.AccessManager.Feature
import com.appifyhub.monolith.service.access.AccessManager.Feature.BASIC
import com.appifyhub.monolith.service.access.AccessManager.Feature.DEMO
import com.appifyhub.monolith.service.access.AccessManager.Feature.MESSAGE_TEMPLATES
import com.appifyhub.monolith.service.access.AccessManager.Feature.USERS
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.silent
import com.appifyhub.monolith.util.ext.throwLocked
import com.appifyhub.monolith.util.ext.throwNotVerified
import com.appifyhub.monolith.util.ext.throwPreconditionFailed
import com.appifyhub.monolith.util.ext.throwUnauthorized
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class AccessManagerImpl(
  private val authService: AuthService,
  private val userService: UserService,
  private val creatorService: CreatorService,
) : AccessManager {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun requestUserAccess(authData: Authentication, targetId: UserId, privilege: Privilege): User {
    log.debug("Authentication $authData requesting '${privilege.name}' access to user $targetId")

    // validate request data and token
    val normalizedTargetId = Normalizers.UserId.run(targetId).requireValid { "User ID" }
    val jwt = authService.requireValidJwt(authData, shallow = false)
    val tokenDetails = authService.fetchTokenDetails(jwt)

    // allow request if it's the project creator requesting
    val isRequesterProjectCreator = fetchCreator(normalizedTargetId.projectId)?.id == tokenDetails.ownerId
    val isRequesterSuperCreator = getSuperCreator().id == tokenDetails.ownerId
    if (isRequesterProjectCreator || isRequesterSuperCreator) return fetchUser(normalizedTargetId)

    // not project creator; validate that the project matches
    val targetProject = fetchProject(normalizedTargetId.projectId)
    val isSameProjectRequest = targetProject.id == tokenDetails.projectId
    require(isSameProjectRequest) { "Only requests within the same project are allowed" }

    // self access is sometimes allowed, check if that's the case
    val requesterUser = fetchUser(tokenDetails.ownerId)
    if (requesterUser.isSelfAccessAllowed(privilege, normalizedTargetId)) return requesterUser

    // fail if user is not verified
    if (!requesterUser.isVerified) throwNotVerified()

    // static tokens are always allowed (unless it's creators looking for other creators)
    val isCreatorRequest = getCreatorProject().id == requesterUser.id.projectId

    @Suppress("KotlinConstantConditions") // keeping for clarity... it's unclear anyway :)
    val isCrossCreatorAccess = !isRequesterSuperCreator && isCreatorRequest
    if (tokenDetails.isStatic && !isCrossCreatorAccess) return fetchUser(normalizedTargetId)

    // check if minimum authorization level is met
    val isPrivileged = requesterUser.isPrivilegedTo(privilege, targetProject)
    require(isPrivileged) { "Only ${privilege.level.groupName} are authorized" }

    // check if authorization level is enough (mods can't access other mods)
    val targetUser = fetchUser(normalizedTargetId)
    val isHigherAuthority = requesterUser.authority.ordinal > targetUser.authority.ordinal
    require(isHigherAuthority) { "Only ${targetUser.authority.nextGroupName} are authorized" }

    return fetchUser(normalizedTargetId)
  }

  override fun requestProjectAccess(authData: Authentication, targetId: Long, privilege: Privilege): Project {
    log.debug("Authentication $authData requesting '${privilege.name}' access to project $targetId")

    // validate request data and token
    val normalizedTargetId = Normalizers.ProjectId.run(targetId).requireValid { "Project ID" }
    val jwt = authService.requireValidJwt(authData, shallow = false)
    val tokenDetails = authService.fetchTokenDetails(jwt)

    // allow request if it's the project creator requesting
    val targetProject = fetchProject(normalizedTargetId)
    val isRequesterProjectCreator = fetchCreator(normalizedTargetId)?.id == tokenDetails.ownerId
    val isRequesterSuperCreator = getSuperCreator().id == tokenDetails.ownerId
    if (isRequesterProjectCreator || isRequesterSuperCreator) return targetProject

    // not project creator; validate that the project matches
    val isSameProjectRequest = tokenDetails.projectId == normalizedTargetId
    require(isSameProjectRequest) { "Only requests within the same project are allowed" }

    // fail if user is not verified
    val requesterUser = fetchUser(tokenDetails.ownerId)
    if (!requesterUser.isVerified) throwNotVerified()

    // static tokens are always allowed (unless it's creators looking for other creators)
    val isCreatorRequest = getCreatorProject().id == requesterUser.id.projectId

    @Suppress("KotlinConstantConditions") // keeping for clarity... it's unclear anyway :)
    val isCrossCreatorAccess = !isRequesterSuperCreator && isCreatorRequest
    if (tokenDetails.isStatic && !isCrossCreatorAccess) return targetProject

    // check if minimum authorization level is met
    val isPrivileged = requesterUser.isPrivilegedTo(privilege, targetProject)
    require(isPrivileged) { "Only ${privilege.level.groupName} are authorized" }

    return targetProject
  }

  override fun requestCreator(authData: Authentication, matchesId: UserId?, requireVerified: Boolean): User {
    log.debug(
      "Authentication $authData requesting creator access," +
        " matchingId = $matchesId, mustBeVerified = $requireVerified"
    )

    // validate request data and token
    val jwt = authService.requireValidJwt(authData, shallow = false)
    val tokenDetails = authService.fetchTokenDetails(jwt)

    val isRequesterCreator = getCreatorProject().id != tokenDetails.ownerId.projectId
    if (isRequesterCreator)
      throwUnauthorized { "Only requests from creators are allowed" }

    matchesId?.let {
      val isSuperCreator = getSuperCreator().id == tokenDetails.ownerId
      val isMatchingId = it == tokenDetails.ownerId
      if (!isSuperCreator && !isMatchingId)
        throwUnauthorized { "Only requests from ${it.toUniversalFormat()} are allowed" }
    }

    val creator = fetchUser(matchesId ?: tokenDetails.ownerId)
    if (requireVerified && !creator.isVerified)
      throwUnauthorized { "Requester must be verified" }

    return creator
  }

  override fun requestSuperCreator(authData: Authentication): User {
    log.debug("Authentication $authData requesting super creator access")

    // validate request data and token
    val jwt = authService.requireValidJwt(authData, shallow = false)
    val tokenDetails = authService.fetchTokenDetails(jwt)

    // allow request if it's the project creator requesting
    val isRequesterSuperCreator = getSuperCreator().id == tokenDetails.ownerId
    if (!isRequesterSuperCreator) throwUnauthorized { "Only requests from super creator are allowed" }

    return fetchUser(tokenDetails.ownerId)
  }

  override fun fetchProjectState(targetId: Long): ProjectState {
    log.debug("Fetching project state for $targetId")

    return resolveProjectSetupStatus(targetId)
  }

  override fun requireProjectFunctional(targetId: Long) {
    log.debug("Requiring project ready $targetId")

    resolveProjectSetupStatus(targetId).requireFunctional()
  }

  override fun requireProjectFeaturesFunctional(targetId: Long, vararg features: Feature) {
    log.debug("Requiring project features ready ${features.joinToString()} for $targetId")

    val setupStatus = resolveProjectSetupStatus(targetId)

    @Suppress("ConvertArgumentToSet") // order matters, features are sorted by importance
    val requestedUnusableFeatures = setupStatus.unusableFeatures.intersect(features.toList())

    if (requestedUnusableFeatures.isNotEmpty())
      throwPreconditionFailed { "Not configured: ${requestedUnusableFeatures.joinToString()}" }
  }

  // Helpers

  private fun ProjectState.requireFunctional() {
    if (project.status != Project.Status.ACTIVE)
      throwPreconditionFailed { "Project is set to '${project.status}' state" }

    // all required features must be usable
    unusableFeatures.firstOrNull { it.isRequired }?.let { requiredFeature ->
      throwPreconditionFailed { "Project feature '$requiredFeature' is not configured" }
    }

    // on hold property must be set to false
    if (project.onHold) throwLocked { "Project is 'on hold'" }
  }

  private fun resolveProjectSetupStatus(targetId: Long): ProjectState {
    val normalizedTargetId = Normalizers.ProjectId.run(targetId).requireValid { "Project ID" }
    val project = fetchProject(normalizedTargetId)

    // A feature is "supported" when either is true:
    //   - feature requires no additional properties to be set
    //   - feature has all of its required properties set

    val featureSupport: Map<Feature, Boolean> =
      Feature.values().associateWith { feature ->
        when (feature) {
          BASIC -> true // always supported
          USERS -> true // always supported
          MESSAGE_TEMPLATES -> true // always supported
          DEMO -> false // unsupported for now
        }
      }

    return ProjectState(
      project = project,
      usableFeatures = featureSupport.filterValues { it }.keys.toList(),
      unusableFeatures = featureSupport.filterValues { !it }.keys.toList(),
    )
  }

  private fun User.isPrivilegedTo(privilege: Privilege, project: Project): Boolean =
    when (privilege) {
      // handle special cases first
      Privilege.USER_SEARCH -> project.anyoneCanSearch || this.authority.ordinal >= privilege.level.ordinal
      // not a special case
      else -> this.authority.ordinal >= privilege.level.ordinal
    }

  private fun User.isSelfAccessAllowed(privilege: Privilege, targetId: UserId): Boolean =
    when (privilege) {
      // secure user properties can only be changed by other high-level users (even for self)
      Privilege.USER_WRITE_AUTHORITY,
      Privilege.USER_WRITE_VERIFICATION,
      -> false

      // non-secure user properties can be used for self
      Privilege.USER_SEARCH,
      Privilege.USER_READ_TOKEN,
      Privilege.USER_READ_DATA,
      Privilege.USER_WRITE_TOKEN,
      Privilege.USER_WRITE_DATA,
      Privilege.USER_WRITE_SIGNATURE,
      Privilege.USER_DELETE,
      -> this.id == targetId

      // invalid group for user
      Privilege.PROJECT_READ,
      Privilege.PROJECT_WRITE,
      -> error("Users should not ask for self-access on projects")

      // invalid group for user
      Privilege.MESSAGE_TEMPLATE_READ,
      Privilege.MESSAGE_TEMPLATE_WRITE,
      -> error("Users should not ask for self-access on message templates")
    }

  private fun getCreatorProject() = creatorService.getCreatorProject()

  private fun getSuperCreator() = creatorService.getSuperCreator()

  private fun fetchCreator(projectId: Long) = silent(log = false) { creatorService.fetchProjectCreator(projectId) }

  private fun fetchUser(id: UserId) = userService.fetchUserByUserId(id)

  private fun fetchProject(id: Long) = creatorService.fetchProjectById(id)

  private val TokenDetails.projectId get() = ownerId.projectId

}
