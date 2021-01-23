package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.auth.AuthRepository
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.service.user.UserServiceImpl.UserPrivilege
import com.appifyhub.monolith.service.validation.Normalizers
import com.appifyhub.monolith.util.requireValid
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
  private val authRepository: AuthRepository,
  private val userService: UserService,
  private val adminService: AdminService,
  private val passwordEncoder: PasswordEncoder,
) : AuthService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun isAuthorized(
    authData: Authentication,
    forAuthority: Authority,
    shallow: Boolean,
  ): Boolean {
    try {
      log.debug("Checking if authorized $authData for $forAuthority")

      if (forAuthority == Authority.DEFAULT) return true // basic authority is always ok
      val token = authData.requireValidJwt(shallow)

      val shallowUser = authRepository.resolveShallowUser(token)
      return shallowUser.isAuthorizedFor(forAuthority)
    } catch (t: Throwable) {
      log.warn("Failed isAuthorized check", t)
      return false
    }
  }

  override fun isProjectOwner(
    authData: Authentication,
    projectSignature: String,
    shallow: Boolean,
  ): Boolean = try {
    log.debug("Checking if $authData is project owner for $projectSignature")

    val normalizedSignature = Normalizers.Dense.run(projectSignature).requireValid { "Project Signature" }
    val token = authData.requireValidJwt(shallow)

    val projectAccountId = adminService.fetchProjectBySignature(normalizedSignature).account.id
    val userAccountId = authRepository.resolveShallowUser(token).account?.id ?: -1
    projectAccountId == userAccountId
  } catch (t: Throwable) {
    log.warn("Failed isProjectOwner check", t)
    false
  }

  override fun resolveShallowUser(authData: Authentication): User {
    log.debug("Fetching user by authentication $authData")
    val token = authData.requireValidJwt(shallow = false)
    return authRepository.resolveShallowUser(token)
  }

  override fun authUser(identifier: String, signature: String, projectSignature: String): User {
    log.debug("Fetching user by $projectSignature, id $identifier, signature $signature")

    val normalizedIdentifier = Normalizers.CustomUserId.run(identifier).requireValid { "Identifier" }
    val normalizedRawSignature = Normalizers.RawSignature.run(signature).requireValid { "Signature" }
    val normalizedProjectSignature = Normalizers.Dense.run(projectSignature).requireValid { "Signature" }

    val project = adminService.fetchProjectBySignature(normalizedProjectSignature)
    return fetchUserByCredentials(project.id, normalizedIdentifier, normalizedRawSignature)
  }

  override fun authAdmin(identifier: String, signature: String): User {
    log.debug("Fetching account by id $identifier, signature $signature")

    val normalizedIdentifier = Normalizers.CustomUserId.run(identifier).requireValid { "Identifier" }
    val normalizedRawSignature = Normalizers.RawSignature.run(signature).requireValid { "Signature" }

    val project = adminService.fetchAdminProject()
    return fetchUserByCredentials(project.id, normalizedIdentifier, normalizedRawSignature)
  }

  override fun createTokenFor(
    user: User,
    origin: String?,
  ): String {
    log.debug("Generating token for $user, origin $origin")

    val normalizedOrigin = Normalizers.Origin.run(origin).requireValid { "Origin" }

    return authRepository.createToken(
      userId = user.userId,
      authorities = user.allAuthorities,
      origin = normalizedOrigin,
    )
  }

  override fun refreshAuth(authData: Authentication): String {
    log.debug("Refreshing authentication $authData")
    val token = authData.requireValidJwt(shallow = false)

    // unauthorize current token
    authRepository.unauthorizeToken(token)

    // create a new token for this user
    val ownedToken = authRepository.fetchTokenDetails(token)
    return authRepository.createToken(
      userId = ownedToken.owner.userId,
      authorities = ownedToken.owner.allAuthorities,
      origin = ownedToken.origin,
    )
  }

  override fun fetchTokenDetails(authData: Authentication): OwnedToken {
    log.debug("Fetching token details for authentication $authData")
    val token = authData.requireValidJwt(shallow = false)
    return authRepository.fetchTokenDetails(token)
  }

  override fun fetchAllTokenDetails(authData: Authentication, valid: Boolean?): List<OwnedToken> {
    log.debug("Fetching all token details for authentication $authData [valid $valid]")
    val token = authData.requireValidJwt(shallow = false)
    return authRepository.fetchAllTokenDetails(token, valid)
  }

  override fun fetchAllTokenDetailsFor(authData: Authentication, userId: UserId, valid: Boolean?): List<OwnedToken> {
    log.debug("Fetching all token details for user $userId [valid $valid]")

    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    authData.requireValidJwt(shallow = false)

    return authRepository.fetchAllTokenDetailsFor(normalizedUserId, valid)
  }

  override fun unauthorize(authData: Authentication) {
    log.debug("Unauthorizing user by authentication $authData")
    val token = authData.requireValidJwt(shallow = true)
    authRepository.unauthorizeToken(token)
  }

  override fun unauthorizeAll(authData: Authentication) {
    log.debug("Unauthorizing all access for authentication $authData")
    val token = authData.requireValidJwt(shallow = false)
    authRepository.unauthorizeAllTokens(token)
  }

  override fun unauthorizeAllFor(authData: Authentication, userId: UserId) {
    log.debug("Unauthorizing all access for $userId")

    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    authData.requireValidJwt(shallow = false)

    authRepository.unauthorizeAllTokensFor(normalizedUserId)
  }

  override fun unauthorizeTokens(authData: Authentication, tokenLocators: List<String>) {
    log.debug("Unauthorizing all access $tokenLocators")

    val normalizedTokens = tokenLocators.map {
      Normalizers.Dense.run(it).requireValid { "Token ID" }
    }
    authData.requireValidJwt(shallow = false)

    val tokens = normalizedTokens.map(::Token)
    authRepository.unauthorizeAllTokens(tokens)
  }

  override fun requestAccessFor(authData: Authentication, targetUserId: UserId, privilege: UserPrivilege): User {
    log.debug("Authentication $authData requesting '${privilege.name}' access to $targetUserId")
    adminService.fetchProjectById(targetUserId.projectId) // sanity check

    val normalizedUserId = Normalizers.UserId.run(targetUserId).requireValid { "User ID" }
    val token = authData.requireValidJwt(shallow = false)

    // quick check to prevent unnecessary queries
    val shallowRequester = authRepository.resolveShallowUser(token)
    val isSelf = shallowRequester.userId == normalizedUserId
    val isPrivilegedShallow = shallowRequester.isAuthorizedFor(privilege.level)
    require(isSelf || isPrivilegedShallow) { "Only ${privilege.level.groupName} are authorized" }

    // fetch non-shallow data for requester
    val requester = userService.fetchUserByUserId(shallowRequester.userId, withTokens = false)
    if (isSelf) return requester

    // check minimum authorization level, as creds might have changed
    val isPrivileged = requester.isAuthorizedFor(privilege.level)
    require(isPrivileged) { "Only ${privilege.level.groupName} are authorized" }

    // check if authorization level is enough (mods can't access other mods)
    val target = userService.fetchUserByUserId(normalizedUserId, withTokens = false)
    val isHigherAuthority = requester.authority.ordinal > target.authority.ordinal
    require(isHigherAuthority) { "Only ${target.authority.nextGroupName} are authorized" }

    return target
  }

  // Helpers

  @Throws
  private fun fetchUserByCredentials(
    projectId: Long,
    identifier: String,
    signature: String,
  ): User {
    val userId = UserId(id = identifier, projectId = projectId)
    val user = userService.fetchUserByUserId(userId, withTokens = false)
    if (!passwordEncoder.matches(signature, user.signature)) {
      log.warn("Password mismatch for $userId")
      throw IllegalArgumentException("Invalid credentials")
    }
    return user
  }

  private fun Authentication.requireValidJwt(shallow: Boolean): JwtAuthenticationToken =
    try {
      (this as JwtAuthenticationToken).apply {
        try {
          authRepository.requireValid(this, shallow)
        } catch (t: Throwable) {
          throw IllegalAccessException(t.message).initCause(t)
        }
      }
    } catch (t: Throwable) {
      log.warn("Wrong token type $this")
      if (t is TypeCastException) throw IllegalAccessException(t.message) else throw t
    }

}