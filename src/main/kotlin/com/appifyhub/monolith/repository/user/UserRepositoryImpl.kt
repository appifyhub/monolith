package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.admin.Project.UserIdType
import com.appifyhub.monolith.domain.mapper.applyTo
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.mapper.toUser
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.auth.TokenDetailsRepository
import com.appifyhub.monolith.storage.dao.ProjectDao
import com.appifyhub.monolith.storage.dao.UserDao
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
  private val userDao: UserDao,
  private val tokenDetailsRepository: TokenDetailsRepository,
  private val projectDao: ProjectDao,
  private val passwordEncoder: PasswordEncoder,
  private val timeProvider: TimeProvider,
  private val springSecurityUserManager: SpringSecurityUserManager,
) : UserRepository,
  SpringSecurityUserManager by springSecurityUserManager {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addUser(creator: UserCreator, userIdType: UserIdType): User {
    log.debug("Adding user by creator $creator")
    if (creator.userId == null && userIdType != UserIdType.RANDOM)
      throw IllegalArgumentException("Missing user ID for creator $creator")
    if (creator.userId != null && userIdType == UserIdType.RANDOM)
      throw IllegalArgumentException("Provided user ID instead of keeping random for creator $creator")

    val user = creator.toUser(
      userId = creator.userId ?: UserIdGenerator.nextId,
      passwordEncoder = passwordEncoder,
      timeProvider = timeProvider,
    ).updateVerificationToken(userIdType)

    return userDao.save(user.toData()).toDomain()
  }

  override fun fetchUserByUserId(id: UserId, withTokens: Boolean): User {
    log.debug("Fetching user $id")
    return fetchUser(id, withTokens)
  }

  override fun fetchUserByUniversalId(universalId: String, withTokens: Boolean): User {
    log.debug("Fetching user $universalId")
    val userId = UserId.fromUniversalFormat(universalId)
    return fetchUser(userId, withTokens)
  }

  override fun fetchAllUsersByContact(contact: String): List<User> {
    log.debug("Fetching all users by contact $contact")
    return userDao.findAllByContact(contact).map(UserDbm::toDomain)
  }

  override fun fetchAllUsersByProjectId(projectId: Long): List<User> {
    log.debug("Fetching all users for project $projectId")
    return userDao.findAllByProject_ProjectId(projectId).map(UserDbm::toDomain)
  }

  override fun updateUser(updater: UserUpdater, userIdType: UserIdType): User {
    log.debug("Updating user $updater")
    val fetchedUser = fetchUser(updater.id, withTokens = false)

    val updatedUser = updater.applyTo(
      user = fetchedUser,
      passwordEncoder = passwordEncoder,
      timeProvider = timeProvider,
    ).updateVerificationToken(userIdType, oldUser = fetchedUser)

    return userDao.save(updatedUser.toData()).toDomain()
  }

  override fun removeUserById(id: UserId) {
    log.debug("Removing user $id")
    userDao.deleteById(id.toData())
  }

  override fun removeUserByUniversalId(universalId: String) {
    log.debug("Removing user $universalId")
    val userId = UserId.fromUniversalFormat(universalId)
    userDao.deleteById(userId.toData())
  }

  override fun removeAllUsersByProjectId(projectId: Long) {
    log.debug("Removing all users from project $projectId")
    return userDao.deleteAllByProject_ProjectId(projectId)
  }

  // Helpers

  @Throws
  private fun fetchUser(id: UserId, withTokens: Boolean): User {
    val user = userDao.findById(id.toData()).get().toDomain()
    if (!withTokens) return user
    val project = projectDao.findById(id.projectId).get().toDomain()
    val allTokenDetails = tokenDetailsRepository.fetchAllTokens(user, project)
    return user.copy(ownedTokens = allTokenDetails)
  }

  private fun User.updateVerificationToken(userIdType: UserIdType, oldUser: User? = null): User = when {
    needsNewEmailToken(userIdType, oldUser) -> copy(verificationToken = TokenGenerator.nextEmailToken)
    needsNewPhoneToken(userIdType, oldUser) -> copy(verificationToken = TokenGenerator.nextPhoneToken)
    else -> this
  }

  private fun User.hasEmailIdChanged(userIdType: UserIdType, oldUser: User? = null): Boolean =
    userIdType == UserIdType.EMAIL && oldUser?.id?.userId != id.userId

  private fun User.hasPhoneIdChanged(userIdType: UserIdType, oldUser: User? = null): Boolean =
    userIdType == UserIdType.PHONE && oldUser?.id?.userId != id.userId

  private fun User.needsNewEmailToken(userIdType: UserIdType, oldUser: User? = null): Boolean {
    // check if contact ID changed
    if (hasEmailIdChanged(userIdType, oldUser)) return true
    // only one verification code exists: if Phone ID changed, assume that Contact Email hasn't
    val hasContactEmailChanged = contactType == ContactType.EMAIL && contact != null && oldUser?.contact != contact
    return hasContactEmailChanged && !hasPhoneIdChanged(userIdType, oldUser)
  }

  private fun User.needsNewPhoneToken(userIdType: UserIdType, oldUser: User? = null): Boolean {
    // check if contact ID changed
    if (hasPhoneIdChanged(userIdType, oldUser)) return true
    // only one verification code exists: if Email ID changed, assume that Contact Phone hasn't
    val hasContactPhoneChanged = contactType == ContactType.PHONE && contact != null && oldUser?.contact != contact
    return hasContactPhoneChanged && !hasEmailIdChanged(userIdType, oldUser)
  }

}
