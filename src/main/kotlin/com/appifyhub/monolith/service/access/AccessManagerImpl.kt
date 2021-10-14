package com.appifyhub.monolith.service.access

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.setup.ProjectStatus
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.access.AccessManager.Feature
import com.appifyhub.monolith.service.access.AccessManager.Privilege
import com.appifyhub.monolith.service.auth.AuthService
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.creator.PropertyService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.silent
import com.appifyhub.monolith.util.ext.throwLocked
import com.appifyhub.monolith.util.ext.throwPreconditionFailed
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class AccessManagerImpl(
  private val authService: AuthService,
  private val userService: UserService,
  private val creatorService: CreatorService,
  private val propService: PropertyService,
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
    val isRequesterSuperOwner = getCreatorOwner().id == tokenDetails.ownerId
    if (isRequesterProjectCreator || isRequesterSuperOwner) return fetchUser(normalizedTargetId)

    // not project creator; validate that the project matches
    val targetProject = fetchProject(normalizedTargetId.projectId)
    val isSameProjectRequest = targetProject.id == tokenDetails.projectId
    require(isSameProjectRequest) { "Only requests within the same project are allowed" }

    // self access is always allowed, check if that's the case
    val requesterUser = fetchUser(tokenDetails.ownerId)
    if (requesterUser.id == normalizedTargetId) return requesterUser

    // static tokens are always allowed (unless it's creators looking for other creators)
    val isCreatorRequest = getCreatorProject().id == requesterUser.id.projectId
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
    log.debug("Authentication $authData requesting '${privilege.name}' access to project $targetId")

    // validate request data and token
    val normalizedTargetId = Normalizers.ProjectId.run(targetId).requireValid { "Project ID" }
    val jwt = authService.requireValidJwt(authData, shallow = false)
    val tokenDetails = authService.fetchTokenDetails(jwt)

    // allow request if it's the project creator requesting
    val isRequesterProjectCreator = fetchCreator(normalizedTargetId)?.id == tokenDetails.ownerId
    val isRequesterSuperOwner = getCreatorOwner().id == tokenDetails.ownerId
    if (isRequesterProjectCreator || isRequesterSuperOwner) return fetchProject(normalizedTargetId)

    // not project creator; validate that the project matches
    val isSameProjectRequest = tokenDetails.projectId == normalizedTargetId
    require(isSameProjectRequest) { "Only requests within the same project are allowed" }

    // static tokens are always allowed (unless it's creators looking for other creators)
    val requesterUser = fetchUser(tokenDetails.ownerId)
    val isCreatorRequest = getCreatorProject().id == requesterUser.id.projectId
    val isCrossCreatorAccess = !isRequesterSuperOwner && isCreatorRequest
    if (tokenDetails.isStatic && !isCrossCreatorAccess) return fetchProject(normalizedTargetId)

    // check if minimum authorization level is met
    val isPrivileged = requesterUser.canActAs(privilege.level)
    require(isPrivileged) { "Only ${privilege.level.groupName} are authorized" }

    return fetchProject(normalizedTargetId)
  }

  override fun requestSuperCreator(authData: Authentication): User {
    log.debug("Authentication $authData requesting super creator access")

    // validate request data and token
    val jwt = authService.requireValidJwt(authData, shallow = false)
    val tokenDetails = authService.fetchTokenDetails(jwt)

    // allow request if it's the project creator requesting
    val isRequesterSuperOwner = getCreatorOwner().id == tokenDetails.ownerId
    require(isRequesterSuperOwner) { "Only requests from super owner are allowed" }

    return fetchUser(tokenDetails.ownerId)
  }

  override fun fetchProjectStatus(targetId: Long): ProjectStatus {
    log.debug("Fetching project status for $targetId")

    return resolveProjectSetupStatus(targetId)
  }

  override fun requireProjectFunctional(targetId: Long) {
    log.debug("Requiring project ready $targetId")

    resolveProjectSetupStatus(targetId).requireFunctional()
  }

  override fun requireProjectFeaturesFunctional(targetId: Long, vararg features: Feature) {
    log.debug("Requiring project features ready ${features.joinToString()} for $targetId")

    val setupStatus = resolveProjectSetupStatus(targetId)
    val requestedUnusableFeatures = setupStatus.unusableFeatures.intersect(features.toList())

    if (requestedUnusableFeatures.isNotEmpty())
      throwPreconditionFailed { "Not configured: ${requestedUnusableFeatures.joinToString()}" }
  }

  // Helpers

  private fun ProjectStatus.requireFunctional() {
    if (status != Project.Status.ACTIVE)
      throwPreconditionFailed { "Project is set to '$status' state" }

    // any required feature must have all of its properties set
    unusableFeatures.firstOrNull { it.isRequired }?.let { requiredFeature ->
      throwPreconditionFailed { "Project feature '$requiredFeature' is not configured" }
    }

    // on hold property must be set to 
    properties.firstOrNull { it.config == ProjectProperty.ON_HOLD }
      .let { (it as? Property.FlagProp)?.typed() ?: true } // assume true if not set
      .let { onHold -> if (onHold) throwLocked { "Project is 'on hold'" } }
  }

  private fun resolveProjectSetupStatus(targetId: Long): ProjectStatus {
    val normalizedTargetId = Normalizers.ProjectId.run(targetId).requireValid { "Project ID" }

    val project = fetchProject(normalizedTargetId)
    val propertyValues = fetchProps(normalizedTargetId)
    val propertyConfigs = propertyValues.map(Property<*>::config)

    // A feature is supported when either is true:
    //   - feature requires no properties
    //   - all required properties are set
    val featureSupport = Feature.values().associateWith { feature ->
      feature.properties.isEmpty() || propertyConfigs.containsAll(feature.properties.toList())
    }

    return ProjectStatus(
      status = project.status,
      usableFeatures = featureSupport.filterValues { it }.keys.toList(),
      unusableFeatures = featureSupport.filterValues { !it }.keys.toList(),
      properties = propertyValues,
    )
  }

  private fun getCreatorProject() = creatorService.getCreatorProject()

  private fun getCreatorOwner() = creatorService.getCreatorOwner()

  private fun fetchCreator(projectId: Long) = silent(log = false) { creatorService.fetchProjectCreator(projectId) }

  private fun fetchUser(id: UserId) = userService.fetchUserByUserId(id)

  private fun fetchProject(id: Long) = creatorService.fetchProjectById(id)

  private fun fetchProps(id: Long) = propService.fetchProperties(id, ProjectProperty.names())

  private val TokenDetails.projectId get() = ownerId.projectId

}
