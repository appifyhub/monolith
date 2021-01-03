package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.auth.AuthRepository
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.assertNotBlank
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
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
    authentication: Authentication,
    forAuthority: Authority,
  ): Boolean = try {
    log.debug("Checking if authorized $authentication for $forAuthority")
    val shallowUser = authRepository.fetchUserByIdentification(authentication, shallow = true)
    shallowUser.isAuthorizedFor(forAuthority)
  } catch (t: Throwable) {
    log.warn("Failed isAuthorized check", t)
    false
  }

  override fun isProjectOwner(
    authentication: Authentication,
    projectSignature: String,
  ): Boolean = try {
    log.debug("Checking if $authentication is project owner for $projectSignature")
    val projectAccountId = adminService.fetchProjectBySignature(projectSignature).account.id
    val userAccountId = authRepository.fetchUserByIdentification(authentication, shallow = true).account?.id ?: -1
    projectAccountId == userAccountId
  } catch (t: Throwable) {
    log.warn("Failed isProjectOwner check", t)
    false
  }

  override fun fetchUserByAuthenticating(authentication: Authentication, shallow: Boolean): User {
    log.debug("Fetching user by authentication $authentication")
    return authRepository.fetchUserByIdentification(authentication, shallow)
  }

  override fun fetchUserByCredentials(
    projectSignature: String,
    identifier: String,
    signature: String,
  ): User {
    log.debug("Fetching user by $projectSignature, id $identifier, signature $signature")

    projectSignature.assertNotBlank(errorName = "Project signature")
    identifier.assertNotBlank(errorName = "ID")
    signature.assertNotBlank(errorName = "Signature")

    val project = adminService.fetchProjectBySignature(projectSignature)
    return fetchUser(project.id, identifier, signature)
  }

  override fun fetchAdminUserByCredentials(
    identifier: String,
    signature: String,
  ): User {
    log.debug("Fetching account by id $identifier, signature $signature")

    identifier.assertNotBlank(errorName = "ID")
    signature.assertNotBlank(errorName = "Signature")

    val project = adminService.fetchAdminProject()
    return fetchUser(project.id, identifier, signature)
  }

  override fun generateTokenFor(
    user: User,
    origin: String?,
  ): String {
    log.debug("Generating token for $user, origin $origin")
    // TODO MM validation missing
    return authRepository.generateToken(user, origin)
  }

  // Helpers

  @Throws
  private fun fetchUser(
    projectId: Long,
    identifier: String,
    signature: String,
  ): User {
    val userId = UserId(id = identifier, projectId = projectId)
    val user = userService.fetchUserByUserId(userId)
    if (!passwordEncoder.matches(signature, user.signature)) {
      log.warn("Password mismatch for $userId")
      throw IllegalArgumentException("Invalid credentials")
    }
    return user
  }

}