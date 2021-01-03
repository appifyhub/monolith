package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.common.stubAccount
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.jwt.JwtHelper
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.mapper.toDomain
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Repository
import org.springframework.security.core.userdetails.User as SpringUser

private const val CLAIM_USER_ID = "userId"
private const val CLAIM_PROJECT_ID = "projectId"
private const val CLAIM_UNIFIED_ID = "unifiedId"
private const val CLAIM_ACCOUNT_ID = "accountId"
private const val CLAIM_AUTHORITIES = "authorities"
private const val CLAIM_ORIGIN = "origin"
private const val AUTHORITY_DELIMITER = ","

@Repository
class AuthRepositoryImpl(
  private val jwtHelper: JwtHelper,
  private val userRepository: UserRepository,
  private val adminRepository: AdminRepository,
  private val timeProvider: TimeProvider,
) : AuthRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun generateToken(user: User, origin: String?): String {
    log.debug("Generating token for user $user")
    val authorities = user.allAuthorities.joinToString(AUTHORITY_DELIMITER) { it.authority }
    val claims = mutableMapOf(
      CLAIM_USER_ID to user.userId.id,
      CLAIM_PROJECT_ID to user.userId.projectId.toString(),
      CLAIM_UNIFIED_ID to user.userId.toUnifiedFormat(),
      CLAIM_AUTHORITIES to authorities,
    ).apply {
      adminRepository.fetchAdminProject()
        .takeIf { it.id == user.userId.projectId }
        ?.let {
          // only add account ID if user is from the admin project
          put(CLAIM_ACCOUNT_ID, user.account!!.id.toString())
        }
      origin?.let { put(CLAIM_ORIGIN, it) }
    }
    val username = user.userId.toUnifiedFormat()
    return jwtHelper.createJwtForClaims(subject = username, claims = claims)
  }

  override fun fetchUserByIdentification(
    authentication: Authentication,
    shallow: Boolean,
  ): User {
    log.debug("Fetching user by credentials $authentication [shallow $shallow]")
    val token = authentication as JwtAuthenticationToken

    if (shallow) {
      // read and parse authorities
      var authorities = token.authorities
      if (authorities.isEmpty()) {
        authorities = token.tokenAttributes[CLAIM_AUTHORITIES].toString()
          .split(AUTHORITY_DELIMITER)
          .map { User.Authority.find(it, User.Authority.DEFAULT) }
          .toSet() // to remove potential duplicates
          .sortedByDescending { it.ordinal }
      }
      // prepare the user model
      val user = SpringUser.builder()
        .username(token.name)
        .password("spring-asks-for-it")
        .authorities(authorities)
        .build()
        .toDomain(timeProvider)

      // user might be from admin project
      val adminProject = adminRepository.fetchAdminProject()
      val projectId = token.tokenAttributes[CLAIM_PROJECT_ID].toString().toLong()
      if (projectId != adminProject.id) return user

      // add admin project info here
      val accountId = token.tokenAttributes[CLAIM_ACCOUNT_ID].toString().toLong()
      return user.copy(
        account = stubAccount().copy(
          id = accountId,
          createdAt = timeProvider.currentDate,
        ),
      )
    } else {
      val userId = UserId.fromUnifiedFormat(token.name)
      return userRepository.fetchUserByUserId(userId)
    }
  }

}