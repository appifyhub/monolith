package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.auth.ops.TokenCreator
import com.appifyhub.monolith.domain.mapper.mergeToString
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.auth.AuthRepository
import com.appifyhub.monolith.repository.geo.GeolocationRepository
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.throwNotVerified
import com.appifyhub.monolith.util.ext.throwUnauthorized
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
  private val authRepository: AuthRepository,
  private val userService: UserService,
  private val creatorService: CreatorService,
  private val geoRepository: GeolocationRepository,
  private val passwordEncoder: PasswordEncoder,
) : AuthService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun requireValidJwt(authData: Authentication, shallow: Boolean): JwtAuthenticationToken {
    log.debug("Requiring valid JWT from $authData, shallow $shallow")
    return authData.forceToJwt(shallow)
  }

  override fun resolveShallowSelf(authData: Authentication): User {
    log.debug("Fetching self by authentication $authData")
    val token = authData.forceToJwt(shallow = true)
    return authRepository.resolveShallowUser(token)
  }

  override fun resolveShallowUser(authData: Authentication, universalId: String): User {
    log.debug("Fetching user by authentication $authData, universalId = $universalId")

    val normalizedUserId = Normalizers.UserId.run(UserId.fromUniversalFormat(universalId)).requireValid { "User ID" }
    val token = authData.forceToJwt(shallow = false)
    val shallowUser = authRepository.resolveShallowUser(token)

    if (normalizedUserId != shallowUser.id) throwUnauthorized { "User ID and auth data mismatch" }

    return shallowUser
  }

  override fun resolveUser(universalId: String, signature: String): User {
    log.debug("Fetching user by $universalId, signature $signature")

    val userId = UserId.fromUniversalFormat(universalId)
    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    val normalizedRawSignature = Normalizers.RawSignature.run(signature).requireValid { "Signature" }

    val user = fetchUserByCredentials(normalizedUserId, normalizedRawSignature)
    if (!user.isVerified) throwNotVerified()

    return user
  }

  override fun resolveCreator(universalId: String, signature: String): User {
    log.debug("Fetching creator by id $universalId, signature $signature")

    val userId = UserId.fromUniversalFormat(universalId)
    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    val normalizedRawSignature = Normalizers.RawSignature.run(signature).requireValid { "Signature" }

    val project = creatorService.getCreatorProject()
    val user = fetchUserByCredentials(normalizedUserId, normalizedRawSignature)

    if (user.id.projectId != project.id) throwUnauthorized { "Project ID" }
    if (!user.isVerified) throwNotVerified()

    return user
  }

  override fun createTokenFor(user: User, origin: String?, ipAddress: String?): String {
    log.debug("Generating token for $user, origin $origin, IP $ipAddress")

    return createToken(user, origin, ipAddress, isStatic = false)
  }

  override fun createStaticTokenFor(user: User, origin: String?, ipAddress: String?): String {
    log.debug("Generating static token for $user, origin $origin, IP $ipAddress")

    return createToken(user, origin, ipAddress, isStatic = true)
  }

  override fun refreshAuth(authData: Authentication, ipAddress: String?): String {
    log.debug("Refreshing authentication $authData")

    val token = authData.forceToJwt(shallow = false)

    val normalizedIp = Normalizers.IpAddress.run(ipAddress).requireValid { "IP Address" }
    if (authRepository.isTokenStatic(token)) throwUnauthorized { "Can't refresh static tokens" }

    // fetch details for this token to reuse them in the new token
    val tokenDetails = authRepository.fetchTokenDetails(token)

    // unauthorize the current token
    authRepository.unauthorizeToken(token)

    // create a new token for this user
    return authRepository.createToken(
      TokenCreator(
        id = tokenDetails.ownerId,
        authority = tokenDetails.authority,
        isStatic = tokenDetails.isStatic,
        origin = tokenDetails.origin,
        ipAddress = normalizedIp,
        geo = geoRepository.fetchGeolocationForIp(normalizedIp)?.mergeToString(),
      )
    ).tokenValue
  }

  override fun fetchTokenDetails(authData: Authentication): TokenDetails {
    log.debug("Fetching token details for authentication $authData")
    val token = authData.forceToJwt(shallow = false)
    return authRepository.fetchTokenDetails(token)
  }

  override fun fetchAllTokenDetails(authData: Authentication, valid: Boolean?): List<TokenDetails> {
    log.debug("Fetching all token details for authentication $authData [valid $valid]")
    val token = authData.forceToJwt(shallow = false)
    return authRepository.fetchAllTokenDetails(token, valid)
  }

  override fun fetchAllTokenDetailsFor(
    authData: Authentication,
    targetId: UserId,
    valid: Boolean?,
  ): List<TokenDetails> {
    log.debug("Fetching all token details for user $targetId [valid $valid]")

    val normalizedUserId = Normalizers.UserId.run(targetId).requireValid { "User ID" }
    authData.forceToJwt(shallow = false)

    return authRepository.fetchAllTokenDetailsFor(normalizedUserId, valid)
  }

  override fun unauthorize(authData: Authentication) {
    log.debug("Unauthorizing user by authentication $authData")
    val token = authData.forceToJwt(shallow = true)
    authRepository.unauthorizeToken(token)
  }

  override fun unauthorizeAll(authData: Authentication) {
    log.debug("Unauthorizing all access for authentication $authData")
    val token = authData.forceToJwt(shallow = false)
    authRepository.unauthorizeAllTokens(token)
  }

  override fun unauthorizeAllFor(authData: Authentication, targetId: UserId) {
    log.debug("Unauthorizing all access for $targetId")

    val normalizedUserId = Normalizers.UserId.run(targetId).requireValid { "User ID" }
    authData.forceToJwt(shallow = false)

    authRepository.unauthorizeAllTokensFor(normalizedUserId)
  }

  override fun unauthorizeTokens(authData: Authentication, tokenValues: List<String>) {
    log.debug("Unauthorizing all access $tokenValues")

    val normalizedTokens = tokenValues.map {
      Normalizers.Dense.run(it).requireValid { "Token ID" }
    }
    authData.forceToJwt(shallow = false)

    authRepository.unauthorizeAllTokens(normalizedTokens)
  }

  // Helpers

  @Throws
  private fun fetchUserByCredentials(id: UserId, signature: String): User {
    val user = userService.fetchUserByUserId(id)
    if (!passwordEncoder.matches(signature, user.signature)) {
      log.warn("Password mismatch for $id")
      throw IllegalArgumentException("Invalid credentials")
    }
    return user
  }

  private fun Authentication.forceToJwt(shallow: Boolean): JwtAuthenticationToken =
    try {
      with(this as JwtAuthenticationToken) {
        if (!authRepository.isTokenValid(this, shallow))
          throw IllegalAccessException("Invalid token for '$name'")
        else this
      }
    } catch (t: Throwable) {
      log.warn("Wrong token type $this")
      if (t is TypeCastException) throw IllegalAccessException(t.message) else throw t
    }

  private fun createToken(user: User, origin: String?, ipAddress: String?, isStatic: Boolean): String {
    val normalizedOrigin = Normalizers.Origin.run(origin).requireValid { "Origin" }
    val normalizedIp = Normalizers.IpAddress.run(ipAddress).requireValid { "IP Address" }
    val geo = geoRepository.fetchGeolocationForIp(normalizedIp)?.mergeToString()

    val isCreator = creatorService.getCreatorProject().id == user.id.projectId
    val isOwner = user.canActAs(Authority.OWNER)
    val isAllowedToCreateStaticTokens = isCreator || isOwner
    if (isStatic && !isAllowedToCreateStaticTokens)
      throwUnauthorized { "Only ${Authority.OWNER.groupName} can create static tokens" }

    return authRepository.createToken(
      TokenCreator(
        id = user.id,
        authority = user.authority,
        isStatic = isStatic,
        origin = normalizedOrigin,
        ipAddress = normalizedIp,
        geo = geo,
      )
    ).tokenValue
  }

}
