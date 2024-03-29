package com.appifyhub.monolith.features.user.repository

import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.features.auth.repository.TokenDetailsRepository
import com.appifyhub.monolith.features.creator.domain.model.Project.UserIdType
import com.appifyhub.monolith.features.user.domain.applyTo
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.User.ContactType
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.features.user.domain.toData
import com.appifyhub.monolith.features.user.domain.toDomain
import com.appifyhub.monolith.features.user.domain.toUser
import com.appifyhub.monolith.features.user.repository.util.SpringSecurityUserManager
import com.appifyhub.monolith.features.user.repository.util.TokenGenerator
import com.appifyhub.monolith.features.user.repository.util.UserIdGenerator
import com.appifyhub.monolith.features.user.storage.UserDao
import com.appifyhub.monolith.features.user.storage.model.UserDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
  private val userDao: UserDao,
  private val tokenDetailsRepository: TokenDetailsRepository,
  private val pushDeviceRepository: PushDeviceRepository,
  private val signupCodeRepository: SignupCodeRepository,
  private val passwordEncoder: PasswordEncoder,
  private val springSecurityUserManager: SpringSecurityUserManager,
  private val timeProvider: TimeProvider,
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

  override fun fetchUserByUserId(id: UserId): User {
    log.debug("Fetching user $id")
    return userDao.findById(id.toData()).get().toDomain()
  }

  override fun fetchUserByUniversalId(universalId: String): User {
    log.debug("Fetching user $universalId")
    val id = UserId.fromUniversalFormat(universalId)
    return userDao.findById(id.toData()).get().toDomain()
  }

  override fun fetchAllUsersByProjectId(projectId: Long): List<User> {
    log.debug("Fetching all users for project $projectId")
    return userDao.findAllByProject_ProjectId(projectId).map(UserDbm::toDomain)
  }

  override fun fetchUserByUserIdAndVerificationToken(userId: UserId, verificationToken: String): User {
    log.debug("Fetching user $userId with token $verificationToken")
    return userDao.findByIdAndVerificationToken(userId.toData(), verificationToken).toDomain()
  }

  override fun searchByName(projectId: Long, name: String): List<User> {
    log.debug("Searching users by name $name in project $projectId")
    return userDao.searchAllByProject_ProjectIdAndNameLike(projectId, name).map(UserDbm::toDomain)
  }

  override fun searchByContact(projectId: Long, contact: String): List<User> {
    log.debug("Searching users by contact $contact in project $projectId")
    return userDao.searchAllByProject_ProjectIdAndContactLike(projectId, contact).map(UserDbm::toDomain)
  }

  override fun countUsers(projectId: Long): Long {
    log.debug("Counting users in project $projectId")
    return userDao.countAllByProject_ProjectId(projectId)
  }

  override fun updateUser(updater: UserUpdater, userIdType: UserIdType): User {
    log.debug("Updating user $updater")

    val user = userDao.findById(updater.id.toData()).get().toDomain()
    val updatedUser = updater.applyTo(
      user = user,
      passwordEncoder = passwordEncoder,
      timeProvider = timeProvider,
    ).updateVerificationToken(userIdType, oldUser = user)

    return userDao.save(updatedUser.toData()).toDomain()
  }

  override fun removeUserById(id: UserId) {
    log.debug("Removing user $id")

    userDao
      .findById(id.toData())
      .get()
      .toDomain()
      .deleteCascadingDependants()

    userDao.deleteById(id.toData())
  }

  override fun removeUserByUniversalId(universalId: String) {
    log.debug("Removing user $universalId")
    val userId = UserId.fromUniversalFormat(universalId)

    userDao
      .findById(userId.toData())
      .get()
      .toDomain()
      .deleteCascadingDependants()

    userDao.deleteById(userId.toData())
  }

  override fun removeAllUsersByProjectId(projectId: Long) {
    log.debug("Removing all users from project $projectId")

    userDao.findAllByProject_ProjectId(projectId)
      .map(UserDbm::toDomain)
      .forEach { it.deleteCascadingDependants() }

    userDao.deleteAllByProject_ProjectId(projectId)
  }

  // Helpers

  private fun User.deleteCascadingDependants() {
    // cascade manually for now
    val tokens = tokenDetailsRepository.fetchAllValidTokens(this, project = null)
    tokenDetailsRepository.blockAllTokens(tokens.map(TokenDetails::tokenValue)) // block in case of later error
    tokenDetailsRepository.removeTokensFor(this, project = null)
    pushDeviceRepository.deleteAllDevicesByUser(this)
    signupCodeRepository.deleteAllByOwner(this)
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
