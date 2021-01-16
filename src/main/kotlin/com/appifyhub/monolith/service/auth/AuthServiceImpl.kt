package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.auth.AuthRepository
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.requireNotBlank
import com.appifyhub.monolith.util.requireNullOrNotBlank
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
  ): Boolean = try {
    log.debug("Checking if authorized $authData for $forAuthority")
    val token = authData.requireValidJwt(shallow)
    val shallowUser = authRepository.resolveShallowUser(token)
    shallowUser.isAuthorizedFor(forAuthority)
  } catch (t: Throwable) {
    log.warn("Failed isAuthorized check", t)
    false
  }

  override fun isProjectOwner(
    authData: Authentication,
    projectSignature: String,
    shallow: Boolean,
  ): Boolean = try {
    log.debug("Checking if $authData is project owner for $projectSignature")
    val token = authData.requireValidJwt(shallow)
    val projectAccountId = adminService.fetchProjectBySignature(projectSignature).account.id
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

  override fun authenticateUser(identifier: String, signature: String, projectSignature: String): User {
    log.debug("Fetching user by $projectSignature, id $identifier, signature $signature")

    projectSignature.requireNotBlank { "Project signature" }
    identifier.requireNotBlank { "ID" }
    signature.requireNotBlank { "Signature" }

    val project = adminService.fetchProjectBySignature(projectSignature)
    return fetchUserByCredentials(project.id, identifier, signature)
  }

  override fun authenticateAdmin(identifier: String, signature: String): User {
    log.debug("Fetching account by id $identifier, signature $signature")

    identifier.requireNotBlank { "ID" }
    signature.requireNotBlank { "Signature" }

    val project = adminService.fetchAdminProject()
    return fetchUserByCredentials(project.id, identifier, signature)
  }

  override fun createTokenFor(
    user: User,
    origin: String?,
  ): String {
    log.debug("Generating token for $user, origin $origin")

    origin.requireNullOrNotBlank { "Origin" }

    return authRepository.createToken(
      userId = user.userId,
      authorities = user.allAuthorities,
      origin = origin,
    )
  }

  override fun refreshAuthentication(authData: Authentication): String {
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

  override fun unauthorizeAuthentication(authData: Authentication) {
    log.debug("Unauthorizing user by authentication $authData")
    val token = authData.requireValidJwt(shallow = true)
    return authRepository.unauthorizeToken(token)
  }

  override fun unauthorizeAllAuthentication(authData: Authentication) {
    log.debug("Unauthorizing all access for authentication $authData")
    val token = authData.requireValidJwt(shallow = false)
    return authRepository.unauthorizeAllTokens(token)
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

  private fun Authentication.requireValidJwt(shallow: Boolean) = try {
    (this as JwtAuthenticationToken).apply {
      try {
        authRepository.requireValid(this, shallow)
      } catch (t: Throwable) {
        throw IllegalAccessException(t.message)
      }
    }
  } catch (t: Throwable) {
    log.warn("Wrong token type $this")
    throw IllegalAccessException("Wrong token type")
  }

}