package com.appifyhub.monolith.util

import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.auth.OwnedTokenRepository
import com.appifyhub.monolith.repository.auth.locator.TokenLocator
import com.appifyhub.monolith.repository.auth.locator.TokenLocatorEncoder
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.security.JwtHelper
import com.appifyhub.monolith.util.ext.silent
import com.auth0.jwt.JWT
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.Calendar

@Component
@Profile(TestAppifyHubApplication.PROFILE)
class AuthTestHelper {

  @Autowired lateinit var userRepo: UserRepository
  @Autowired lateinit var adminRepo: AdminRepository
  @Autowired lateinit var ownedTokenRepo: OwnedTokenRepository
  @Autowired lateinit var tokenEncoder: TokenLocatorEncoder
  @Autowired lateinit var timeProvider: TimeProvider
  @Autowired lateinit var jwtHelper: JwtHelper

  val adminProject: Project by lazy { adminRepo.getAdminProject() }
  val ownerUser: User by lazy { userRepo.fetchAllUsersByProjectId(adminProject.id).first() }
  val defaultUser: User
    get() = ensureUser(Authority.DEFAULT)
  val moderatorUser: User
    get() = ensureUser(Authority.MODERATOR)
  val adminUser: User
    get() = ensureUser(Authority.ADMIN)
  var expirationDaysDelta: Int = 1

  fun newStubToken() = convertTokenToJwt(createStubToken())

  fun newRealToken(authority: Authority) = convertTokenToJwt(createUserToken(authority))

  private fun createStubToken(): String = newToken(
    user = Stubs.user,
    storeOwnedToken = false,
    accountId = Stubs.account.id,
  )

  private fun createUserToken(authority: Authority) = ensureUser(authority).let { user ->
    newToken(
      user = user,
      storeOwnedToken = true,
      accountId = user.account?.id,
    )
  }

  private fun convertTokenToJwt(token: String): JwtAuthenticationToken =
    JWT.decode(token).let { decoded ->
      val jwt = Jwt(
        /* tokenValue = */
        token,
        /* issuedAt = */
        decoded.issuedAt.toInstant(),
        /* expiresAt = */
        decoded.expiresAt.toInstant(),
        /* headers = */
        mapOf("typ" to "JWT", "alg" to "RS256"),
        /* claims = */
        decoded.claims.mapValues { it.value.asString() },
      )
      val authorities = jwt.claims["authorities"].toString()
        .split(",")
        .map { Authority.find(it, Authority.DEFAULT) }
        .toSet() // to remove potential duplicates
        .sortedByDescending { it.ordinal }
      JwtAuthenticationToken(jwt, authorities, jwt.subject)
    }

  // it's almost the same as what Auth does
  private fun newToken(
    user: User,
    storeOwnedToken: Boolean,
    accountId: Long? = null,
  ): String = with(timeProvider) {
    val now = currentMillis // avoid double call, 2 values would be returned
    val tokenLocator = tokenEncoder.encode(
      TokenLocator(
        userId = user.userId,
        origin = Stubs.userCredentialsRequest.origin,
        timestamp = now,
      )
    )

    if (storeOwnedToken) {
      currentCalendar
        .apply { timeInMillis = now }
        .let { cal ->
          ownedTokenRepo.addToken(
            userId = user.userId,
            token = Token(tokenLocator),
            createdAt = cal.time,
            expiresAt = cal.apply { add(Calendar.DAY_OF_MONTH, expirationDaysDelta) }.time,
            origin = Stubs.userCredentialsRequest.origin,
          )
        }
    }

    jwtHelper.createJwtForClaims(
      subject = user.userId.toUniversalFormat(),
      claims = mutableMapOf(
        "userId" to user.userId.id,
        "projectId" to user.userId.projectId.toString(),
        "universalId" to user.userId.toUniversalFormat(),
        "authorities" to user.allAuthorities.joinToString(",") { it.authority },
        "tokenLocator" to tokenLocator,
        "origin" to Stubs.userCredentialsRequest.origin!!
      ).apply {
        accountId?.let { put("accountId", accountId.toString()) }
      },
      expirationDaysDelta = expirationDaysDelta,
    )
  }

  fun ensureUser(authority: Authority): User =
    when (authority) {
      Authority.OWNER -> ownerUser
      else -> silent {
        userRepo.addUser(
          creator = Stubs.userCreator.copy(
            id = "username_${authority.name.toLowerCase()}",
            projectId = adminProject.id,
            type = User.Type.PERSONAL,
            authority = authority,
          ),
          userIdType = Project.UserIdType.USERNAME,
        )
      } ?: userRepo.fetchUserByUserId(
        userId = UserId("username_${authority.name.toLowerCase()}", adminProject.id),
        withTokens = false,
      )
    }

}
