package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.common.stubAccount
import com.appifyhub.monolith.domain.common.stubProject
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Repository
import java.util.Calendar
import java.util.Date
import org.springframework.security.core.userdetails.User as SpringUser

private const val CLAIM_USER_ID = "userId"
private const val CLAIM_PROJECT_ID = "projectId"
private const val CLAIM_UNIFIED_ID = "unifiedId"
private const val CLAIM_AUTHORITIES = "authorities"
private const val CLAIM_ACCOUNT_ID = "accountId"
private const val CLAIM_ORIGIN = "origin"
private const val CLAIM_TOKEN_LOCATOR = "tokenLocator"
private const val AUTHORITY_DELIMITER = ","

@Repository
class AuthRepositoryImpl(
  private val jwtHelper: JwtHelper,
  private val userRepository: UserRepository,
  private val adminRepository: AdminRepository,
  private val ownedTokenRepository: OwnedTokenRepository,
  private val tokenLocatorEncoder: TokenLocatorEncoder,
  private val tokenLocatorDecoder: TokenLocatorDecoder,
  private val timeProvider: TimeProvider,
) : AuthRepository {

  @Value("\${app.security.jwt.default-expiration-days}")
  private var defaultExpirationDays: Int = 1

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun createToken(userId: UserId, authorities: List<GrantedAuthority>, origin: String?): String {
    log.debug("Generating token for user $userId, origin $origin")

    // prepare claim data
    val authoritiesEncoded = authorities.joinToString(AUTHORITY_DELIMITER) { it.authority }
    val username = userId.toUnifiedFormat()
    val tokenLocator = TokenLocator(userId = userId, origin = origin, timestamp = timeProvider.currentMillis)
    val tokenLocatorEncoded = tokenLocatorEncoder.encode(tokenLocator)

    // create claims
    val claims = mutableMapOf(
      CLAIM_USER_ID to userId.id,
      CLAIM_PROJECT_ID to userId.projectId.toString(),
      CLAIM_UNIFIED_ID to username,
      CLAIM_AUTHORITIES to authoritiesEncoded,
      CLAIM_TOKEN_LOCATOR to tokenLocatorEncoded,
    ).apply {
      // add account ID if user is from the admin project
      adminRepository.getAdminProject()
        .takeIf { adminProject -> adminProject.id == userId.projectId }
        ?.let { userRepository.fetchUserByUserId(userId, withTokens = false).account }
        ?.let { account -> put(CLAIM_ACCOUNT_ID, account.id.toString()) }

      // put origin if available
      origin?.let { put(CLAIM_ORIGIN, it) }
    }

    val now = timeProvider.currentCalendar.time
    val expiration = timeProvider.currentCalendar.apply {
      add(Calendar.DAY_OF_MONTH, defaultExpirationDays)
    }.time
    ownedTokenRepository.addToken(
      userId = userId,
      token = Token(tokenLocatorEncoded),
      createdAt = now,
      expiresAt = expiration,
      origin = origin,
    )
    return jwtHelper.createJwtForClaims(subject = username, claims = claims)
  }

  override fun checkIsValid(token: JwtAuthenticationToken, shallow: Boolean): Boolean {
    log.debug("Checking if token is valid $token [shallow $shallow]")

    if (shallow) return !token.isExpired

    val isBlocked = ownedTokenRepository.checkIsBlocked(Token(token.locator))
    if (isBlocked) return false

    val isExpired = ownedTokenRepository.checkIsExpired(Token(token.locator))
    return !isExpired
  }

  override fun requireValid(token: JwtAuthenticationToken, shallow: Boolean) {
    log.debug("Requiring valid token $token [shallow $shallow]")

    if (shallow) require(!token.isExpired) { "Token expired ${token.secondsUntilExpired} seconds ago" }

    val isBlocked = ownedTokenRepository.checkIsBlocked(Token(token.locator))
    require(!isBlocked) { "Token is blocked" }

    val isExpired = ownedTokenRepository.checkIsExpired(Token(token.locator))
    require(!isExpired) { "Token expired ${token.secondsUntilExpired} seconds ago" }
  }

  override fun resolveShallowUser(token: JwtAuthenticationToken): User {
    log.debug("Fetching user by token $token")
    // parse everything and stub data for shallow user

    // read and parse authorities
    var authorities = token.authorities
    if (authorities.isEmpty()) {
      authorities = token.customAuthorities
        .split(AUTHORITY_DELIMITER)
        .map { User.Authority.find(it, User.Authority.DEFAULT) }
        .toSet() // to remove potential duplicates
        .sortedByDescending { it.ordinal }
    }

    // prepare the user model
    var user = SpringUser.builder()
      .username(token.name)
      .password("spring-asks-for-it")
      .authorities(authorities)
      .build()
      .toDomain(timeProvider)

    // add the current token, assume it's not blocked or expired for shallow fetch
    val tokenLocatorEncoded = token.locator
    val tokenLocator = tokenLocatorDecoder.decode(tokenLocatorEncoded)
    val tokenDate = Date(tokenLocator.timestamp)
    user = user.copy(
      ownedTokens = listOf(
        OwnedToken(
          token = Token(tokenLocatorEncoded),
          isBlocked = false,
          origin = tokenLocator.origin,
          createdAt = tokenDate,
          expiresAt = tokenDate,
          owner = user,
        ),
      ),
    )

    // add admin project info here (user might be from admin project)
    val adminProject = adminRepository.getAdminProject()
    val projectId = token.projectId.toLong()
    if (projectId != adminProject.id) return user
    val accountId = token.accountId.toLong()
    user = user.copy(
      account = stubAccount().copy(
        id = accountId,
        createdAt = timeProvider.currentDate,
      ),
    )

    return user
  }

  override fun fetchTokenDetails(token: JwtAuthenticationToken): OwnedToken {
    log.debug("Fetching token details for $token")
    return ownedTokenRepository.fetchTokenDetails(Token(token.locator))
  }

  override fun fetchAllTokenDetails(token: JwtAuthenticationToken, valid: Boolean?): List<OwnedToken> {
    log.debug("Fetching all token details for $token [valid $valid]")
    val userId = UserId.fromUnifiedFormat(token.name)
    val user = userRepository.fetchUserByUserId(userId, withTokens = false)
    val project = stubProject().copy(id = userId.projectId)
    return when (valid) {
      true -> ownedTokenRepository.fetchAllValidTokens(user, project)
      false -> ownedTokenRepository.fetchAllBlockedTokens(user, project)
      null -> ownedTokenRepository.fetchAllTokens(user, project)
    }
  }

  override fun fetchAllTokenDetailsFor(userId: UserId, valid: Boolean?): List<OwnedToken> {
    log.debug("Fetching all token details for $userId [valid $valid]")
    val user = userRepository.fetchUserByUserId(userId, withTokens = false)
    val project = stubProject().copy(id = userId.projectId)
    return when (valid) {
      true -> ownedTokenRepository.fetchAllTokens(user, project)
      false -> ownedTokenRepository.fetchAllBlockedTokens(user, project)
      null -> ownedTokenRepository.fetchAllValidTokens(user, project)
    }
  }

  override fun unauthorizeToken(token: JwtAuthenticationToken) {
    log.debug("Unauthorizing token $token")
    ownedTokenRepository.blockToken(Token(token.locator))
  }

  override fun unauthorizeAllTokens(token: JwtAuthenticationToken) {
    log.debug("Unauthorizing all tokens with $token")
    val userId = UserId.fromUnifiedFormat(token.name)
    val user = userRepository.fetchUserByUserId(userId, withTokens = true)
    ownedTokenRepository.blockAllTokensFrom(user)
  }

  override fun unauthorizeAllTokensFor(userId: UserId) {
    log.debug("Unauthorizing all tokens for $userId")
    val user = userRepository.fetchUserByUserId(userId, withTokens = true)
    ownedTokenRepository.blockAllTokensFrom(user)
  }

  override fun unauthorizeAllTokens(tokens: List<Token>) {
    log.debug("Unauthorizing all tokens $tokens")
    ownedTokenRepository.blockAllTokens(tokens)
  }

  // Helpers

  private val JwtAuthenticationToken.projectId: String
    get() = tokenAttributes[CLAIM_PROJECT_ID].toString()

  private val JwtAuthenticationToken.customAuthorities: String
    get() = tokenAttributes[CLAIM_AUTHORITIES].toString()

  private val JwtAuthenticationToken.accountId: String
    get() = tokenAttributes[CLAIM_ACCOUNT_ID].toString()

  private val JwtAuthenticationToken.locator: String
    get() = tokenAttributes[CLAIM_TOKEN_LOCATOR].toString()

  private val JwtAuthenticationToken.secondsUntilExpired: Long
    get() {
      val now = timeProvider.currentInstant
      val expiresAt = token.expiresAt ?: now
      return expiresAt.epochSecond - now.epochSecond
    }

  private val JwtAuthenticationToken.isExpired: Boolean
    get() = secondsUntilExpired < 0

}