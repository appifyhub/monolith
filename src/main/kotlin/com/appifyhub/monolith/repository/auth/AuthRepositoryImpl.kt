package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.common.stubAccount
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.jwt.JwtHelper
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.auth.locator.TokenLocator
import com.appifyhub.monolith.repository.auth.locator.TokenLocatorDecoder
import com.appifyhub.monolith.repository.auth.locator.TokenLocatorEncoder
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Repository
import java.util.Date
import org.springframework.security.core.userdetails.User as SpringUser

private const val CLAIM_USER_ID = "userId"
private const val CLAIM_PROJECT_ID = "projectId"
private const val CLAIM_UNIFIED_ID = "unifiedId"
private const val CLAIM_ACCOUNT_ID = "accountId"
private const val CLAIM_AUTHORITIES = "authorities"
private const val CLAIM_ORIGIN = "origin"
private const val CLAIM_TOKEN_LOCATOR = "tokenLocator"
private const val AUTHORITY_DELIMITER = ","

@Repository
class AuthRepositoryImpl(
  private val jwtHelper: JwtHelper,
  private val userRepository: UserRepository,
  private val adminRepository: AdminRepository,
  private val timeProvider: TimeProvider,
  private val tokenLocatorEncoder: TokenLocatorEncoder,
  private val tokenLocatorDecoder: TokenLocatorDecoder,
  private val ownedTokenRepository: OwnedTokenRepository,
) : AuthRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun generateToken(user: User, origin: String?): String {
    log.debug("Generating token for user $user")

    // prepare claim data
    val authorities = user.allAuthorities.joinToString(AUTHORITY_DELIMITER) { it.authority }
    val username = user.userId.toUnifiedFormat()
    val tokenLocator = TokenLocator(userId = user.userId, origin = origin, timestamp = timeProvider.currentMillis)
    val tokenLocatorEncoded = tokenLocatorEncoder.encode(tokenLocator)

    // create claims
    val claims = mutableMapOf(
      CLAIM_USER_ID to user.userId.id,
      CLAIM_PROJECT_ID to user.userId.projectId.toString(),
      CLAIM_UNIFIED_ID to user.userId.toUnifiedFormat(),
      CLAIM_AUTHORITIES to authorities,
      CLAIM_TOKEN_LOCATOR to tokenLocatorEncoded,
    ).apply {
      // put admin account if available
      adminRepository.fetchAdminProject()
        .takeIf { it.id == user.userId.projectId }
        ?.let {
          // only add account ID if user is from the admin project
          put(CLAIM_ACCOUNT_ID, user.account!!.id.toString())
        }

      // put origin if available
      origin?.let { put(CLAIM_ORIGIN, it) }
    }

    ownedTokenRepository.addToken(user, Token(tokenLocatorEncoded), origin)
    return jwtHelper.createJwtForClaims(subject = username, claims = claims)
  }

  override fun fetchUserByAuthenticationData(authData: Authentication, shallow: Boolean): User {
    log.debug("Fetching user by credentials $authData [shallow $shallow]")
    val authorization = authData as JwtAuthenticationToken

    if (!shallow) {
      val userId = UserId.fromUnifiedFormat(authorization.name)
      return userRepository.fetchUserByUserId(userId, withTokens = true)
    }

    // shallow mode: parse everything and stub data...
    // read and parse authorities
    var authorities = authorization.authorities
    if (authorities.isEmpty()) {
      authorities = authorization.tokenAttributes[CLAIM_AUTHORITIES].toString()
        .split(AUTHORITY_DELIMITER)
        .map { User.Authority.find(it, User.Authority.DEFAULT) }
        .toSet() // to remove potential duplicates
        .sortedByDescending { it.ordinal }
    }

    // prepare the user model
    var user = SpringUser.builder()
      .username(authorization.name)
      .password("spring-asks-for-it")
      .authorities(authorities)
      .build()
      .toDomain(timeProvider)

    // add the current token, assume it's not blocked for shallow fetch
    val tokenLocatorEncoded = authorization.tokenAttributes[CLAIM_TOKEN_LOCATOR].toString()
    val decoded = tokenLocatorDecoder.decode(tokenLocatorEncoded)
    user = user.copy(
      ownedTokens = listOf(
        OwnedToken(
          token = Token(tokenLocatorEncoded),
          isBlocked = false,
          origin = decoded.origin,
          createdAt = Date(decoded.timestamp),
          owner = user,
        ),
      ),
    )

    // user might be from admin project (this fetch is instant)
    // add admin project info here
    val adminProject = adminRepository.fetchAdminProject()
    val projectId = authorization.tokenAttributes[CLAIM_PROJECT_ID].toString().toLong()
    if (projectId != adminProject.id) return user
    val accountId = authorization.tokenAttributes[CLAIM_ACCOUNT_ID].toString().toLong()
    user = user.copy(
      account = stubAccount().copy(
        id = accountId,
        createdAt = timeProvider.currentDate,
      ),
    )

    return user
  }

  override fun unauthorizeAuthenticationData(authData: Authentication) {
    log.debug("Unauthorizing credentials $authData")
    val authorization = authData as JwtAuthenticationToken

    val tokenLocatorEncoded = authorization.tokenAttributes[CLAIM_TOKEN_LOCATOR].toString()

    ownedTokenRepository.blockToken(Token(tokenLocatorEncoded))
  }

}