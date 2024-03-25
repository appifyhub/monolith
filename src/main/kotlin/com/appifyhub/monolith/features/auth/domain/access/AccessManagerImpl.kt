package com.appifyhub.monolith.features.auth.domain.access

import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature.BASIC
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature.EMAILS
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature.PUSH
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature.SMS
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature.USERS
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Privilege
import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.features.auth.domain.service.AuthService
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectState
import com.appifyhub.monolith.features.creator.domain.service.CreatorService
import com.appifyhub.monolith.features.user.domain.service.UserService
import com.appifyhub.monolith.util.extension.requireValid
import com.appifyhub.monolith.util.extension.silent
import com.appifyhub.monolith.util.extension.throwLocked
import com.appifyhub.monolith.util.extension.throwNotVerified
import com.appifyhub.monolith.util.extension.throwPreconditionFailed
import com.appifyhub.monolith.util.extension.throwUnauthorized
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
    requireForAuth(isSameProjectRequest) { "Only requests within the same project are allowed" }

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
    requireForAuth(isPrivileged) { "Only ${privilege.level.groupName} are authorized" }

    // check if authorization level is enough (mods can't access other mods)
    val targetUser = fetchUser(normalizedTargetId)
    val isHigherAuthority = requesterUser.authority.ordinal > targetUser.authority.ordinal
    requireForAuth(isHigherAuthority) { "Only ${targetUser.authority.nextGroupName} are authorized" }

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
    requireForAuth(isSameProjectRequest) { "Only requests within the same project are allowed" }

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
    requireForAuth(isPrivileged) { "Only ${privilege.level.groupName} are authorized" }

    return targetProject
  }

  override fun requestCreator(authData: Authentication, matchesId: UserId?, requireVerified: Boolean): User {
    log.debug(
      "Authentication $authData requesting creator access," +
        " matchingId = $matchesId, mustBeVerified = $requireVerified",
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
          EMAILS -> project.mailgunConfig != null
          SMS -> project.twilioConfig != null
          PUSH -> project.firebaseConfig != null
        }
      }

    return ProjectState(
      project = project,
      usableFeatures = featureSupport.filterValues { it }.keys.toList(),
      unusableFeatures = featureSupport.filterValues { !it }.keys.toList(),
    )
  }

  private fun User.isPrivilegedTo(privilege: Privilege, project: Project): Boolean {
    // first check the basic authorization level
    val isAuthorizedByLevel = this.authority.ordinal >= privilege.level.ordinal
    if (isAuthorizedByLevel) return true

    // and then the special cases
    if (privilege == Privilege.USER_SEARCH) return project.anyoneCanSearch

    return false
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
      Privilege.USER_READ_PUSH_DEVICE,
      Privilege.USER_READ_DATA,
      Privilege.USER_READ_SIGNUP_CODE,
      Privilege.USER_WRITE_TOKEN,
      Privilege.USER_WRITE_DATA,
      Privilege.USER_WRITE_SIGNATURE,
      Privilege.USER_WRITE_PUSH_DEVICE,
      Privilege.USER_WRITE_SIGNUP_CODE,
      Privilege.USER_DELETE_PUSH_DEVICE,
      Privilege.USER_DELETE,
      Privilege.MESSAGE_TEMPLATE_SEND,
      -> this.id == targetId

      // invalid group for user
      Privilege.PROJECT_READ,
      Privilege.PROJECT_WRITE,
      Privilege.PROJECT_READ_BASIC,
      -> error("Users should not ask for self-access on projects")

      // invalid group for user
      Privilege.MESSAGE_TEMPLATE_READ,
      Privilege.MESSAGE_TEMPLATE_WRITE,
      -> error("Users should not ask for self-access on message templates")
    }

  private fun requireForAuth(value: Boolean, lazyMessage: () -> Any) =
    if (!value) throwUnauthorized(lazyMessage) else Unit

  private fun getCreatorProject() = creatorService.getCreatorProject()

  private fun getSuperCreator() = creatorService.getSuperCreator()

  private fun fetchCreator(projectId: Long) = silent(log = false) { creatorService.fetchProjectCreator(projectId) }

  private fun fetchUser(id: UserId) = userService.fetchUserByUserId(id)

  private fun fetchProject(id: Long) = creatorService.fetchProjectById(id)

  private val TokenDetails.projectId get() = ownerId.projectId

}
