package com.appifyhub.monolith.util

import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.TokenDetails
import com.appifyhub.monolith.domain.mapper.toTokenDetails
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.auth.AuthRepository
import com.appifyhub.monolith.repository.auth.TokenDetailsRepository
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.security.JwtHelper
import com.appifyhub.monolith.security.JwtHelper.Claims
import com.appifyhub.monolith.util.ext.silent
import com.auth0.jwt.JWT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.Date
import java.util.concurrent.TimeUnit

@Component
@Profile(TestAppifyHubApplication.PROFILE)
class AuthTestHelper {

  @Autowired lateinit var userRepo: UserRepository
  @Autowired lateinit var adminRepo: AdminRepository
  @Autowired lateinit var authRepo: AuthRepository
  @Autowired lateinit var tokenDetailsRepo: TokenDetailsRepository
  @Autowired lateinit var timeProvider: TimeProvider
  @Autowired lateinit var jwtHelper: JwtHelper

  var expirationDaysDelta: Long = 1
  val adminProject: Project by lazy { adminRepo.getAdminProject() }
  val ownerUser: User by lazy { userRepo.fetchAllUsersByProjectId(adminProject.id).first() }
  val defaultUser: User
    get() = ensureUser(Authority.DEFAULT)
  val moderatorUser: User
    get() = ensureUser(Authority.MODERATOR)
  val adminUser: User
    get() = ensureUser(Authority.ADMIN)

  fun newStubToken() = convertTokenToJwt(createStubToken())

  fun newRealToken(authority: Authority) = convertTokenToJwt(createUserToken(authority))

  fun fetchLastTokenOf(user: User): TokenDetails =
    tokenDetailsRepo.fetchAllTokens(
      owner = user,
      project = adminRepo.fetchProjectById(user.id.projectId),
    ).maxByOrNull { it.createdAt }!!

  fun fetchTokenDetailsFor(tokenValue: String): TokenDetails =
    jwtHelper.extractPropertiesFromJwt(tokenValue).toTokenDetails()

  fun isAuthorized(jwt: JwtAuthenticationToken) = authRepo.checkIsValid(jwt, shallow = false)

  fun isAuthorized(tokenValue: String) = authRepo.checkIsValid(convertTokenToJwt(tokenValue), shallow = false)

  private fun createStubToken(): String = newToken(
    user = Stubs.user,
    storeTokenDetails = false,
    accountId = Stubs.account.id,
  )

  private fun createUserToken(authority: Authority) = ensureUser(authority)
    .let { user ->
      newToken(
        user = user,
        storeTokenDetails = true,
        accountId = user.account?.id,
      )
    }

  private fun convertTokenToJwt(tokenValue: String): JwtAuthenticationToken =
    JWT.decode(tokenValue).let { decoded ->
      val jwt = Jwt(
        tokenValue, // tokenValue
        decoded.issuedAt.toInstant(), // issuedAt
        decoded.expiresAt.toInstant(), // expiresAt
        mapOf("typ" to "JWT", "alg" to "RS256"), // headers
        decoded.claims.mapValues { it.value.asString() }, // claims
      )
      val authorities = jwt.claims[Claims.AUTHORITIES].toString()
        .split(",")
        .map { Authority.find(it, Authority.DEFAULT) }
        .toSet() // to remove potential duplicates
        .sortedByDescending { it.ordinal }
      JwtAuthenticationToken(jwt, authorities, jwt.subject)
    }

  // it's almost the same as what Auth does
  private fun newToken(
    user: User,
    storeTokenDetails: Boolean,
    accountId: Long? = null,
    isStatic: Boolean = false,
  ): String = with(timeProvider) {
    // avoid double time call, 2 values would be returned from a mock provider
    val now = currentMillis
    val exp = now + TimeUnit.DAYS.toMillis(expirationDaysDelta)

    val tokenValue = jwtHelper.createJwtForClaims(
      subject = user.id.toUniversalFormat(),
      claims = mutableMapOf(
        Claims.USER_ID to user.id.userId,
        Claims.PROJECT_ID to user.id.projectId.toString(),
        Claims.UNIVERSAL_ID to user.id.toUniversalFormat(),
        Claims.AUTHORITIES to user.allAuthorities.joinToString(",") { it.authority },
        Claims.ORIGIN to Stubs.userCredentialsRequest.origin!!,
        Claims.IS_STATIC to isStatic,
      ).apply {
        accountId?.let { put(Claims.ACCOUNT_ID, accountId.toString()) }
      },
      createdAt = Date(now),
      expiresAt = Date(exp),
    )

    if (storeTokenDetails) {
      tokenDetailsRepo.addToken(TokenDetails(
        tokenValue = tokenValue,
        isBlocked = false,
        createdAt = Date(now),
        expiresAt = Date(exp),
        ownerId = user.id,
        authority = user.authority,
        origin = Stubs.userCredentialsRequest.origin!!,
        ipAddress = null,
        geo = null,
        accountId = accountId,
        isStatic = isStatic,
      ))
    }

    tokenValue
  }

  fun ensureUser(authority: Authority): User =
    when (authority) {
      Authority.OWNER -> ownerUser
      else -> silent {
        userRepo.addUser(
          creator = Stubs.userCreator.copy(
            userId = "username_${authority.name.lowercase()}",
            projectId = adminProject.id,
            type = User.Type.PERSONAL,
            authority = authority,
          ),
          userIdType = Project.UserIdType.USERNAME,
        )
      } ?: userRepo.fetchUserByUserId(
        id = UserId("username_${authority.name.lowercase()}", adminProject.id),
        withTokens = false,
      )
    }

}
