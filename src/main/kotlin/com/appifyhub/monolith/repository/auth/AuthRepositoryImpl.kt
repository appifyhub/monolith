package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.auth.ops.TokenCreator
import com.appifyhub.monolith.domain.common.stubAccount
import com.appifyhub.monolith.domain.common.stubProject
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.mapper.toTokenDetails
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.security.JwtHelper
import com.appifyhub.monolith.security.JwtHelper.Claims.ACCOUNT_ID
import com.appifyhub.monolith.security.JwtHelper.Claims.AUTHORITIES
import com.appifyhub.monolith.security.JwtHelper.Claims.AUTHORITY_DELIMITER
import com.appifyhub.monolith.security.JwtHelper.Claims.GEO
import com.appifyhub.monolith.security.JwtHelper.Claims.IP_ADDRESS
import com.appifyhub.monolith.security.JwtHelper.Claims.IS_STATIC
import com.appifyhub.monolith.security.JwtHelper.Claims.ORIGIN
import com.appifyhub.monolith.security.JwtHelper.Claims.PROJECT_ID
import com.appifyhub.monolith.security.JwtHelper.Claims.UNIVERSAL_ID
import com.appifyhub.monolith.security.JwtHelper.Claims.USER_ID
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Repository
import java.util.Calendar
import kotlin.math.absoluteValue
import org.springframework.security.core.userdetails.User as SpringUser

@Repository
class AuthRepositoryImpl(
  private val jwtHelper: JwtHelper,
  private val userRepository: UserRepository,
  private val adminRepository: AdminRepository,
  private val tokenDetailsRepository: TokenDetailsRepository,
  private val timeProvider: TimeProvider,
) : AuthRepository {

  @Value("\${app.security.jwt.default-expiration-days}")
  private var expirationInDays: Int = 1

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun createToken(creator: TokenCreator): TokenDetails {
    log.debug("Generating token for creator $creator")

    // prepare token data
    val universalId = creator.userId.toUniversalFormat()
    val currentCalendar = timeProvider.currentCalendar
    val expirationCalendar = timeProvider.currentCalendar.apply { add(Calendar.DAY_OF_MONTH, expirationInDays) }
    val authoritiesEncoded = creator.authority.allAuthorities.joinToString(AUTHORITY_DELIMITER) { it.authority }
    val accountId = adminRepository.getAdminProject()
      .takeIf { adminProject -> adminProject.id == creator.userId.projectId }
      ?.let { userRepository.fetchUserByUserId(creator.userId, withTokens = false).account?.id }

    // create claims
    val claims = mutableMapOf(
      USER_ID to creator.userId.id,
      PROJECT_ID to creator.userId.projectId,
      UNIVERSAL_ID to universalId,
      AUTHORITIES to authoritiesEncoded,
      IS_STATIC to creator.isStatic,
    ).apply {
      // put nullable properties only if available
      accountId?.let { put(ACCOUNT_ID, it) }
      creator.ipAddress?.let { put(IP_ADDRESS, it) }
      creator.geo?.let { put(GEO, it) }
      creator.origin?.let { put(ORIGIN, it) }
    }

    val tokenValue = jwtHelper.createJwtForClaims(
      subject = universalId,
      createdAt = currentCalendar.time,
      expiresAt = expirationCalendar.time,
      claims = claims,
    )

    return tokenDetailsRepository.addToken(
      TokenDetails(
        tokenValue = tokenValue,
        isBlocked = false,
        createdAt = currentCalendar.time,
        expiresAt = expirationCalendar.time,
        ownerId = creator.userId,
        authority = creator.authority,
        origin = creator.origin,
        ipAddress = creator.ipAddress,
        geo = creator.geo,
        accountId = accountId,
        isStatic = creator.isStatic,
      )
    )
  }

  override fun checkIsValid(jwt: JwtAuthenticationToken, shallow: Boolean): Boolean {
    log.debug("Checking if token is valid $jwt [shallow $shallow]")

    if (shallow) return !jwt.isExpired

    val isBlocked = tokenDetailsRepository.checkIsBlocked(jwt.token.tokenValue)
    if (isBlocked) return false

    val isExpired = tokenDetailsRepository.checkIsExpired(jwt.token.tokenValue)
    return !isExpired
  }

  override fun requireValid(jwt: JwtAuthenticationToken, shallow: Boolean) {
    log.debug("Requiring valid token $jwt [shallow $shallow]")

    if (shallow) {
      require(!jwt.isExpired) { "Token expired ${jwt.secondsUntilExpired.absoluteValue} seconds ago" }
      return
    }

    val isBlocked = tokenDetailsRepository.checkIsBlocked(jwt.token.tokenValue)
    require(!isBlocked) { "Token is blocked" }

    val isExpired = tokenDetailsRepository.checkIsExpired(jwt.token.tokenValue)
    require(!isExpired) { "Token expired ${jwt.secondsUntilExpired.absoluteValue} seconds ago" }
  }

  override fun resolveShallowUser(jwt: JwtAuthenticationToken): User {
    log.debug("Fetching user by token $jwt")

    val tokenDetails = jwtHelper.extractPropertiesFromJwt(jwt.token.tokenValue).toTokenDetails()

    // prepare the base user model
    var user = SpringUser.builder()
      .username(tokenDetails.ownerId.toUniversalFormat())
      .password("spring-asks-for-it")
      .authorities(tokenDetails.authority.allAuthorities)
      .build()
      .toDomain(timeProvider)

    // add the current token, have to assume it's not blocked for shallow fetch
    user = user.copy(ownedTokens = listOf(tokenDetails))

    // for users who are not from admin project just return
    val isNotFromAdminProject = tokenDetails.ownerId.projectId != adminRepository.getAdminProject().id
    if (tokenDetails.accountId == null || isNotFromAdminProject) return user

    user = user.copy(
      account = stubAccount().copy(
        id = tokenDetails.accountId,
        createdAt = timeProvider.currentDate,
        updatedAt = timeProvider.currentDate,
      ),
    )

    return user
  }

  override fun fetchTokenDetails(jwt: JwtAuthenticationToken): TokenDetails {
    log.debug("Fetching token details for $jwt")
    return tokenDetailsRepository.fetchTokenDetails(jwt.token.tokenValue)
  }

  override fun fetchAllTokenDetails(jwt: JwtAuthenticationToken, valid: Boolean?): List<TokenDetails> {
    log.debug("Fetching all token details for $jwt [valid $valid]")
    val userId = UserId.fromUniversalFormat(jwt.name)
    val user = userRepository.fetchUserByUserId(userId, withTokens = false)
    val project = stubProject().copy(id = userId.projectId)
    return when (valid) {
      true -> tokenDetailsRepository.fetchAllValidTokens(user, project)
      false -> tokenDetailsRepository.fetchAllBlockedTokens(user, project)
      null -> tokenDetailsRepository.fetchAllTokens(user, project)
    }
  }

  override fun fetchAllTokenDetailsFor(userId: UserId, valid: Boolean?): List<TokenDetails> {
    log.debug("Fetching all token details for $userId [valid $valid]")
    val user = userRepository.fetchUserByUserId(userId, withTokens = false)
    val project = stubProject().copy(id = userId.projectId)
    return when (valid) {
      true -> tokenDetailsRepository.fetchAllValidTokens(user, project)
      false -> tokenDetailsRepository.fetchAllBlockedTokens(user, project)
      null -> tokenDetailsRepository.fetchAllTokens(user, project)
    }
  }

  override fun unauthorizeToken(jwt: JwtAuthenticationToken) {
    log.debug("Unauthorizing token $jwt")
    tokenDetailsRepository.blockToken(jwt.token.tokenValue)
  }

  override fun unauthorizeAllTokens(jwt: JwtAuthenticationToken) {
    log.debug("Unauthorizing all tokens with $jwt")
    val userId = UserId.fromUniversalFormat(jwt.name)
    val user = userRepository.fetchUserByUserId(userId, withTokens = true)
    tokenDetailsRepository.blockAllTokensFromModel(user)
  }

  override fun unauthorizeAllTokensFor(userId: UserId) {
    log.debug("Unauthorizing all tokens for $userId")
    val user = userRepository.fetchUserByUserId(userId, withTokens = true)
    tokenDetailsRepository.blockAllTokensFromModel(user)
  }

  override fun unauthorizeAllTokens(tokenValues: List<String>) {
    log.debug("Unauthorizing all tokens $tokenValues")
    tokenDetailsRepository.blockAllTokens(tokenValues)
  }

  // Helpers

  private val JwtAuthenticationToken.secondsUntilExpired: Long
    get() {
      val now = timeProvider.currentInstant
      val expiresAt = token.expiresAt ?: now
      return expiresAt.epochSecond - now.epochSecond
    }

  private val JwtAuthenticationToken.isExpired: Boolean
    get() = secondsUntilExpired < 0

}
