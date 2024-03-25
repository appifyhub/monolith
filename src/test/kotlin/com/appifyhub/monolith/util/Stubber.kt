package com.appifyhub.monolith.util

import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.ProjectCreator
import com.appifyhub.monolith.features.auth.domain.toTokenDetails
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.User.Authority
import com.appifyhub.monolith.features.user.domain.model.User.Authority.ADMIN
import com.appifyhub.monolith.features.user.domain.model.User.Authority.DEFAULT
import com.appifyhub.monolith.features.user.domain.model.User.Authority.MODERATOR
import com.appifyhub.monolith.features.user.domain.model.User.Authority.OWNER
import com.appifyhub.monolith.features.user.domain.model.User.ContactType
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.features.auth.repository.AuthRepository
import com.appifyhub.monolith.features.auth.repository.TokenDetailsRepository
import com.appifyhub.monolith.features.creator.repository.CreatorRepository
import com.appifyhub.monolith.features.user.repository.UserRepository
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper.Claims
import com.appifyhub.monolith.util.extension.silent
import com.appifyhub.monolith.util.extension.takeIfNotBlank
import com.auth0.jwt.JWT
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val EXPIRATION_DAYS_DELTA: Long = 1

@Component
@Profile(TestAppifyHubApplication.PROFILE)
class Stubber(
  private val userRepo: UserRepository,
  private val creatorRepo: CreatorRepository,
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
    project = creatorRepo.fetchProjectById(user.id.projectId),
  ).maxByOrNull { it.createdAt }!!

  fun tokenDetailsOf(tokenValue: String): TokenDetails =
    jwtHelper.extractPropertiesFromJwt(tokenValue).toTokenDetails()

  @Suppress("unused")
  fun isAuthorized(jwt: JwtAuthenticationToken) = authRepo.isTokenValid(jwt, shallow = false)

  fun isAuthorized(tokenValue: String) = authRepo.isTokenValid(tokenValue.toJwt(), shallow = false)

  // region API models

  inner class Projects {
    fun creator() = creatorRepo.getCreatorProject()

    fun new(
      owner: User = creators.owner(),
      userIdType: Project.UserIdType = Project.UserIdType.USERNAME,
      status: Project.Status = Project.Status.ACTIVE,
      activateNow: Boolean = false,
      anyoneCanSearch: Boolean = true,
      maxUsers: Int = 1000,
      name: String = "${owner.name}'s Stub Project",
      language: String = Locale.US.toLanguageTag(),
      requiresSignupCodes: Boolean = false,
      maxSignupCodesPerUser: Int = Integer.MAX_VALUE,
    ) = creatorRepo.addProject(
      ProjectCreator(
        owner = owner,
        type = Project.Type.COMMERCIAL,
        status = status,
        userIdType = userIdType,
        name = name,
        description = "${owner.name}'s Stub Project's Description",
        logoUrl = "https://www.example.com/logo.png",
        websiteUrl = "https://www.example.com",
        maxUsers = maxUsers,
        anyoneCanSearch = anyoneCanSearch,
        onHold = !activateNow,
        languageTag = language,
        requiresSignupCodes = requiresSignupCodes,
        maxSignupCodesPerUser = maxSignupCodesPerUser,
        mailgunConfig = null,
        twilioConfig = null,
        firebaseConfig = null,
      ),
    )

    val all: List<Project>
      get() = creatorRepo.fetchAllProjects()
  }

  inner class Creators {
    fun owner() = creatorRepo.getSuperCreator()
    fun default(
      idSuffix: String = "",
      idReplace: String = "",
      autoVerified: Boolean = true,
      language: String = Locale.US.toLanguageTag(),
      contact: String = "user@example.com",
      contactType: ContactType = ContactType.EMAIL,
    ) = ensureUser(
      DEFAULT,
      project = projects.creator(),
      idSuffix = idSuffix,
      idReplace = idReplace,
      autoVerified = autoVerified,
      language = language,
      contact = contact,
      contactType = contactType,
    )
  }

  inner class Users(private val project: Project) {
    fun owner(
      idSuffix: String = "",
      idReplace: String = "",
      autoVerified: Boolean = true,
      language: String = Locale.US.toLanguageTag(),
      contact: String = "user@example.com",
      contactType: ContactType = ContactType.EMAIL,
    ) = ensureUser(
      authority = OWNER,
      project = project,
      idSuffix = idSuffix,
      idReplace = idReplace,
      autoVerified = autoVerified,
      language = language,
      contact = contact,
      contactType = contactType,
    )

    fun admin(
      idSuffix: String = "",
      idReplace: String = "",
      autoVerified: Boolean = true,
      language: String = Locale.US.toLanguageTag(),
      contact: String = "user@example.com",
      contactType: ContactType = ContactType.EMAIL,
    ) = ensureUser(
      authority = ADMIN,
      project = project,
      idSuffix = idSuffix,
      idReplace = idReplace,
      autoVerified = autoVerified,
      language = language,
      contact = contact,
      contactType = contactType,
    )

    @Suppress("unused")
    fun mod(
      idSuffix: String = "",
      idReplace: String = "",
      autoVerified: Boolean = true,
      language: String = Locale.US.toLanguageTag(),
      contact: String = "user@example.com",
      contactType: ContactType = ContactType.EMAIL,
    ) = ensureUser(
      authority = MODERATOR,
      project = project,
      idSuffix = idSuffix,
      idReplace = idReplace,
      autoVerified = autoVerified,
      language = language,
      contact = contact,
      contactType = contactType,
    )

    fun default(
      idSuffix: String = "",
      idReplace: String = "",
      autoVerified: Boolean = true,
      language: String = Locale.US.toLanguageTag(),
      contact: String = "user@example.com",
      contactType: ContactType = ContactType.EMAIL,
    ) = ensureUser(
      authority = DEFAULT,
      project = project,
      idSuffix = idSuffix,
      idReplace = idReplace,
      autoVerified = autoVerified,
      language = language,
      contact = contact,
      contactType = contactType,
    )
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
      idSuffix: String = "",
      idReplace: String = "",
      autoVerified: Boolean = true,
      language: String = Locale.US.toLanguageTag(),
      contact: String = "user@example.com",
      contactType: ContactType = ContactType.EMAIL,
    ) = createToken(
      user = ensureUser(
        authority = authority,
        project = project,
        idSuffix = idSuffix,
        idReplace = idReplace,
        autoVerified = autoVerified,
        language = language,
        contact = contact,
        contactType = contactType,
      ),
      shouldStore = true,
      isStatic = isStatic,
    ).toJwt()
  }

  inner class UserTokens(private val user: User) {
    fun stub(isStatic: Boolean = false) = createToken(user = user, shouldStore = false, isStatic = isStatic).toJwt()
    fun real(isStatic: Boolean = false) = createToken(user = user, shouldStore = true, isStatic = isStatic).toJwt()
  }

  // endregion

  // region Helpers

  private fun ensureUser(
    authority: Authority,
    project: Project,
    idSuffix: String,
    idReplace: String,
    autoVerified: Boolean,
    language: String,
    contact: String,
    contactType: ContactType,
  ): User = when {
    authority == OWNER && project == projects.creator() -> creators.owner()
    else -> {
      val userId = idReplace.trim().takeIfNotBlank() ?: "username_${authority.name.lowercase()}$idSuffix"
      // silently return null on failure (to simplify things)
      var user = silent {
        userRepo.addUser(
          creator = Stubs.userCreator.copy(
            userId = userId.takeIf { project.userIdType != Project.UserIdType.RANDOM },
            projectId = project.id,
            type = User.Type.PERSONAL,
            authority = authority,
            languageTag = language,
            contact = contact,
            contactType = contactType,
          ),
          userIdType = project.userIdType,
        )
      }
      if (user == null) {
        user = userRepo.fetchUserByUserId(id = UserId(userId, project.id))
      } else if (autoVerified) {
        user = userRepo.updateUser(
          userIdType = project.userIdType,
          updater = UserUpdater(
            id = user.id,
            verificationToken = Settable(null),
          ),
        )
      }
      user
    }
  }

  // it's almost the same as what Auth does
  private fun createToken(
    user: User,
    shouldStore: Boolean,
    isStatic: Boolean,
  ): String {
    val now = timeProvider.currentMillis
    val exp = now + TimeUnit.DAYS.toMillis(EXPIRATION_DAYS_DELTA)

    val tokenValue = jwtHelper.createJwtForClaims(
      subject = user.id.toUniversalFormat(),
      claims = mutableMapOf(
        Claims.USER_ID to user.id.userId,
        Claims.PROJECT_ID to user.id.projectId.toString(),
        Claims.UNIVERSAL_ID to user.id.toUniversalFormat(),
        Claims.AUTHORITIES to user.allAuthorities.joinToString(",") { it.authority },
        Claims.ORIGIN to Stubs.userCredentialsRequest.origin!!,
        Claims.IS_STATIC to (isStatic && user.authority == OWNER),
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
        ),
      )
    }

    return tokenValue
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
        .map { Authority.find(it, DEFAULT) }
        .toSet() // to remove potential duplicates
        .sortedByDescending { it.ordinal }
      JwtAuthenticationToken(jwt, authorities, jwt.subject)
    }

  // endregion

}
