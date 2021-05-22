package com.appifyhub.monolith.service.auth

import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.auth.ops.TokenCreator
import com.appifyhub.monolith.domain.mapper.mergeToString
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.auth.AuthRepository
import com.appifyhub.monolith.repository.geo.GeolocationRepository
import com.appifyhub.monolith.service.admin.AdminService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.service.user.UserService.UserPrivilege
import com.appifyhub.monolith.util.ext.requireValid
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
  private val adminService: AdminService,
  private val geoRepository: GeolocationRepository,
  private val passwordEncoder: PasswordEncoder,
) : AuthService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun hasSelfAuthority(
    authData: Authentication,
    authority: Authority,
    shallow: Boolean,
  ) = try {
    log.debug("Checking if authorized $authData for $authority")

    when (authority) {
      Authority.DEFAULT -> {
        authData.requireValidJwt(shallow = true)
        true // basic authority is always ok
      }
      else -> with(authData.requireValidJwt(shallow)) {
        authRepository.resolveShallowUser(this)
          .canActAs(authority)
      }
    }
  } catch (t: Throwable) {
    log.warn("Failed isAuthorized check", t)
    false
  }

  override fun isProjectOwner(authData: Authentication, shallow: Boolean): Boolean = try {
    log.debug("Checking if $authData is project owner")

    val token = authData.requireValidJwt(shallow)
    val shallowUser = authRepository.resolveShallowUser(token)
    val userAccountId = shallowUser.account?.id ?: -1
    val projectAccountId = adminService.fetchProjectById(shallowUser.id.projectId).account.id

    projectAccountId == userAccountId
  } catch (t: Throwable) {
    log.warn("Failed isProjectOwner check", t)
    false
  }

  override fun resolveShallowSelf(authData: Authentication): User {
    log.debug("Fetching self by authentication $authData")
    val token = authData.requireValidJwt(shallow = true)
    return authRepository.resolveShallowUser(token)
  }

  override fun resolveShallowUser(authData: Authentication, universalId: String): User {
    log.debug("Fetching user by authentication $authData, universalId = $universalId")

    val normalizedUserId = Normalizers.UserId.run(UserId.fromUniversalFormat(universalId)).requireValid { "User ID" }
    val token = authData.requireValidJwt(shallow = false)
    val shallowUser = authRepository.resolveShallowUser(token)

    if (normalizedUserId != shallowUser.id) throwUnauthorized { "User ID and auth data mismatch" }

    return shallowUser
  }

  override fun requestAccessFor(authData: Authentication, targetId: UserId, privilege: UserPrivilege): User {
    log.debug("Authentication $authData requesting '${privilege.name}' access to $targetId")

    // validate request data and token
    val normalizedTargetUserId = Normalizers.UserId.run(targetId).requireValid { "User ID" }
    val jwt = authData.requireValidJwt(shallow = false)
    val tokenDetails = authRepository.fetchTokenDetails(jwt)

    // validate that the project matches
    adminService.fetchProjectById(targetId.projectId) // sanity check for project existence
    val shallowRequester = authRepository.resolveShallowUser(jwt)
    val targetProjectMatches = tokenDetails.ownerId.projectId == targetId.projectId
    val requesterProjectMatches = tokenDetails.ownerId.projectId == shallowRequester.id.projectId
    require(targetProjectMatches && requesterProjectMatches) { "Only requests within the same project are allowed" }

    // fetch requester and target users
    val requesterUser = userService.fetchUserByUserId(shallowRequester.id, withTokens = false)
    val targetUser = userService.fetchUserByUserId(normalizedTargetUserId, withTokens = false)

    // self access is always allowed
    if (requesterUser.id == normalizedTargetUserId) return requesterUser
    // static tokens are always allowed
    if (tokenDetails.isStatic) return targetUser

    // check if minimum authorization level is met
    val isPrivileged = requesterUser.canActAs(privilege.minLevel)
    require(isPrivileged) { "Only ${privilege.minLevel.groupName} are authorized" }

    // check if authorization level is enough (mods can't access other mods)
    val isHigherAuthority = requesterUser.authority.ordinal > targetUser.authority.ordinal
    require(isHigherAuthority) { "Only ${targetUser.authority.nextGroupName} are authorized" }

    return targetUser
  }

  override fun resolveUser(universalId: String, signature: String): User {
    log.debug("Fetching user by $universalId, signature $signature")

    val userId = UserId.fromUniversalFormat(universalId)
    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    val normalizedRawSignature = Normalizers.RawSignature.run(signature).requireValid { "Signature" }

    return fetchUserByCredentials(normalizedUserId, normalizedRawSignature)
  }

  override fun resolveAdmin(universalId: String, signature: String): User {
    log.debug("Fetching admin by id $universalId, signature $signature")

    val userId = UserId.fromUniversalFormat(universalId)
    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    val normalizedRawSignature = Normalizers.RawSignature.run(signature).requireValid { "Signature" }

    val project = adminService.getAdminProject()
    val user = fetchUserByCredentials(normalizedUserId, normalizedRawSignature)

    if (user.id.projectId != project.id) throwUnauthorized { "Project ID" }

    return user
  }

  override fun createTokenFor(user: User, origin: String?, ipAddress: String?): String {
    log.debug("Generating token for $user, origin $origin")

    val normalizedOrigin = Normalizers.Origin.run(origin).requireValid { "Origin" }
    val normalizedIp = Normalizers.IpAddress.run(ipAddress).requireValid { "IP Address" }

    return authRepository.createToken(
      TokenCreator(
        id = user.id,
        authority = user.authority,
        isStatic = false,
        origin = normalizedOrigin,
        ipAddress = normalizedIp,
        geo = geoRepository.fetchGeolocationForIp(normalizedIp)?.mergeToString(),
      )
    ).tokenValue
  }

  override fun createStaticTokenFor(user: User, origin: String?, ipAddress: String?): String {
    TODO("Not yet implemented")
  }

  override fun refreshAuth(authData: Authentication, ipAddress: String?): String {
    log.debug("Refreshing authentication $authData")

    val token = authData.requireValidJwt(shallow = false)
    if (authRepository.isTokenStatic(token)) throwUnauthorized { "Can't refresh static tokens" }

    val normalizedIp = Normalizers.IpAddress.run(ipAddress).requireValid { "IP Address" }

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
    val token = authData.requireValidJwt(shallow = false)
    return authRepository.fetchTokenDetails(token)
  }

  override fun fetchAllTokenDetails(authData: Authentication, valid: Boolean?): List<TokenDetails> {
    log.debug("Fetching all token details for authentication $authData [valid $valid]")
    val token = authData.requireValidJwt(shallow = false)
    return authRepository.fetchAllTokenDetails(token, valid)
  }

  override fun fetchAllTokenDetailsFor(
    authData: Authentication,
    targetId: UserId,
    valid: Boolean?,
  ): List<TokenDetails> {
    log.debug("Fetching all token details for user $targetId [valid $valid]")

    val normalizedUserId = Normalizers.UserId.run(targetId).requireValid { "User ID" }
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

  override fun unauthorizeAllFor(authData: Authentication, targetId: UserId) {
    log.debug("Unauthorizing all access for $targetId")

    val normalizedUserId = Normalizers.UserId.run(targetId).requireValid { "User ID" }
    authData.requireValidJwt(shallow = false)

    authRepository.unauthorizeAllTokensFor(normalizedUserId)
  }

  override fun unauthorizeTokens(authData: Authentication, tokenValues: List<String>) {
    log.debug("Unauthorizing all access $tokenValues")

    val normalizedTokens = tokenValues.map {
      Normalizers.Dense.run(it).requireValid { "Token ID" }
    }
    authData.requireValidJwt(shallow = false)

    authRepository.unauthorizeAllTokens(normalizedTokens)
  }

  // Helpers

  @Throws
  private fun fetchUserByCredentials(
    id: UserId,
    signature: String,
  ): User {
    val user = userService.fetchUserByUserId(id, withTokens = false)
    if (!passwordEncoder.matches(signature, user.signature)) {
      log.warn("Password mismatch for $id")
      throw IllegalArgumentException("Invalid credentials")
    }
    return user
  }

  private fun Authentication.requireValidJwt(shallow: Boolean): JwtAuthenticationToken =
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

}
