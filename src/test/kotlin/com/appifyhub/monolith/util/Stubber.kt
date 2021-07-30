package com.appifyhub.monolith.util

import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.ProjectCreationInfo
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
import com.appifyhub.monolith.util.ext.empty
import com.appifyhub.monolith.util.ext.silent
import com.auth0.jwt.JWT
import java.util.Date
import java.util.concurrent.TimeUnit
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

private const val EXPIRATION_DAYS_DELTA: Long = 1

@Suppress("unused")

@Component
@Profile(TestAppifyHubApplication.PROFILE)
class Stubber(
  private val userRepo: UserRepository,
  private val adminRepo: AdminRepository,
  private val authRepo: AuthRepository,
  private val tokenDetailsRepo: TokenDetailsRepository,
  private val timeProvider: TimeProvider,
  private val jwtHelper: JwtHelper,
) {

  val projects = Projects()
  val creators = Creators()

  fun users(project: Project) = Users(project)
  fun tokens(project: Project) = ProjectTokens(project)
  fun creatorTokens() = ProjectTokens(projects.creator())
  fun tokens(user: User) = UserTokens(user)

  fun latestTokenOf(user: User): TokenDetails = tokenDetailsRepo.fetchAllTokens(
    owner = user,
    project = adminRepo.fetchProjectById(user.id.projectId),
  ).maxByOrNull { it.createdAt }!!

  fun tokenDetailsOf(tokenValue: String): TokenDetails =
    jwtHelper.extractPropertiesFromJwt(tokenValue).toTokenDetails()

  fun isAuthorized(jwt: JwtAuthenticationToken) = authRepo.isTokenValid(jwt, shallow = false)

  fun isAuthorized(tokenValue: String) = authRepo.isTokenValid(tokenValue.toJwt(), shallow = false)

  // API helpers

  inner class Projects {
    fun creator() = adminRepo.getAdminProject()

    fun new(creator: User = creators.owner()) = adminRepo.addProject(
      creationInfo = ProjectCreationInfo(
        type = Project.Type.COMMERCIAL,
        status = Project.Status.ACTIVE,
        userIdType = Project.UserIdType.USERNAME,
      ),
      creator = creator,
    )
  }

  inner class Creators {
    fun owner() = adminRepo.getAdminOwner()
    fun default(idSuffix: String = "") =
      ensureUser(Authority.DEFAULT, project = projects.creator(), idSuffix = idSuffix)
  }

  inner class Users(private val project: Project) {
    fun owner(idSuffix: String = "") = ensureUser(Authority.OWNER, project = project, idSuffix = idSuffix)
    fun admin(idSuffix: String = "") = ensureUser(Authority.ADMIN, project = project, idSuffix = idSuffix)
    fun moderator(idSuffix: String = "") = ensureUser(Authority.MODERATOR, project = project, idSuffix = idSuffix)
    fun default(idSuffix: String = "") = ensureUser(Authority.DEFAULT, project = project, idSuffix = idSuffix)
  }

  inner class ProjectTokens(private val project: Project) {
    fun stub(
      authority: Authority = Stubs.user.authority,
      isStatic: Boolean = false,
    ) = createToken(
      user = Stubs.user.copy(id = Stubs.userId.copy(projectId = project.id), authority = authority),
      shouldStore = false,
      isStatic = isStatic,
    ).toJwt()

    fun real(
      authority: Authority,
      isStatic: Boolean = false,
      idSuffix: String = String.empty,
    ) = createToken(
      user = ensureUser(authority = authority, project = project, idSuffix = idSuffix),
      shouldStore = true,
      isStatic = isStatic,
    ).toJwt()
  }

  inner class UserTokens(private val user: User) {
    fun stub(isStatic: Boolean = false) = createToken(user = user, shouldStore = false, isStatic = isStatic).toJwt()
    fun real(isStatic: Boolean = false) = createToken(user = user, shouldStore = true, isStatic = isStatic).toJwt()
  }

  // Helpers

  private fun ensureUser(
    authority: Authority,
    project: Project,
    idSuffix: String,
  ): User = when {
    authority == Authority.OWNER && project == projects.creator() -> creators.owner()
    else -> {
      val userId = "username_${authority.name.lowercase()}$idSuffix"
      silent {
        // silently return null on failure (to simplify things)
        userRepo.addUser(
          creator = Stubs.userCreator.copy(
            userId = userId,
            projectId = project.id,
            type = User.Type.PERSONAL,
            authority = authority,
          ),
          userIdType = project.userIdType,
        )
      } ?: userRepo.fetchUserByUserId(
        id = UserId(userId, project.id),
        withTokens = false,
      )
    }
  }

  // it's almost the same as what Auth does
  private fun createToken(
    user: User,
    shouldStore: Boolean,
    isStatic: Boolean,
  ): String = with(timeProvider) {
    val now = currentMillis
    val exp = now + TimeUnit.DAYS.toMillis(EXPIRATION_DAYS_DELTA)

    val tokenValue = jwtHelper.createJwtForClaims(
      subject = user.id.toUniversalFormat(),
      claims = mutableMapOf(
        Claims.USER_ID to user.id.userId,
        Claims.PROJECT_ID to user.id.projectId.toString(),
        Claims.UNIVERSAL_ID to user.id.toUniversalFormat(),
        Claims.AUTHORITIES to user.allAuthorities.joinToString(",") { it.authority },
        Claims.ORIGIN to Stubs.userCredentialsRequest.origin!!,
        Claims.IS_STATIC to (isStatic && user.authority == Authority.OWNER),
      ),
      createdAt = Date(now),
      expiresAt = Date(exp),
    )

    if (shouldStore) {
      tokenDetailsRepo.addToken(
        TokenDetails(
          tokenValue = tokenValue,
          isBlocked = false,
          createdAt = Date(now),
          expiresAt = Date(exp),
          ownerId = user.id,
          authority = user.authority,
          origin = Stubs.userCredentialsRequest.origin!!,
          ipAddress = null,
          geo = null,
          isStatic = isStatic,
        )
      )
    }

    tokenValue
  }

  private fun String.toJwt(): JwtAuthenticationToken =
    JWT.decode(this).let { decoded ->
      val jwt = Jwt(
        this, // tokenValue
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

}
