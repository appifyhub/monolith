package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
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
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.auth.OwnedTokenRepository
import com.appifyhub.monolith.storage.dao.UserDao
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
  private val userDao: UserDao,
  private val ownedTokenRepository: OwnedTokenRepository,
  private val adminRepository: AdminRepository,
  private val passwordEncoder: PasswordEncoder,
  private val timeProvider: TimeProvider,
  private val springSecurityUserManager: SpringSecurityUserManager,
) : UserRepository,
  SpringSecurityUserManager by springSecurityUserManager {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addUser(creator: UserCreator, project: Project): User {
    log.debug("Adding user by creator $creator")
    if (creator.id == null && project.userIdType != UserIdType.RANDOM)
      throw IllegalArgumentException("Missing user ID for creator $creator")
    if (creator.id != null && project.userIdType == UserIdType.RANDOM)
      throw IllegalArgumentException("Provided user ID instead of keeping random for creator $creator")

    val user = creator.toUser(
      userId = creator.id ?: UserIdGenerator.nextId,
      passwordEncoder = passwordEncoder,
      timeProvider = timeProvider,
    ).updateVerificationToken(project)

    return userDao.save(user.toData()).toDomain()
  }

  override fun fetchUserByUserId(userId: UserId, withTokens: Boolean): User {
    log.debug("Fetching user $userId")
    return fetchUser(userId, withTokens)
  }

  override fun fetchUserByUnifiedIdFormat(idHashProjectId: String, withTokens: Boolean): User {
    log.debug("Fetching user $idHashProjectId")
    val userId = UserId.fromUnifiedFormat(idHashProjectId)
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

  override fun fetchAllUsersByAccount(account: Account): List<User> {
    log.debug("Fetching all users for account $account")
    return userDao.findAllByAccount(account.toData()).map(UserDbm::toDomain)
  }

  override fun updateUser(updater: UserUpdater, project: Project): User {
    log.debug("Updating user $updater")
    val fetchedUser = fetchUser(updater.id, withTokens = false)

    val updatedUser = updater.applyTo(
      user = fetchedUser,
      passwordEncoder = passwordEncoder,
      timeProvider = timeProvider,
    ).updateVerificationToken(project, oldUser = fetchedUser)

    return userDao.save(updatedUser.toData()).toDomain()
  }

  override fun removeUserById(userId: UserId) {
    log.debug("Removing user $userId")
    userDao.deleteById(userId.toData())
  }

  override fun removeUserByUnifiedFormat(idHashProjectId: String) {
    log.debug("Removing user $idHashProjectId")
    val userId = UserId.fromUnifiedFormat(idHashProjectId)
    userDao.deleteById(userId.toData())
  }

  override fun removeAllUsersByProjectId(projectId: Long) {
    log.debug("Removing all users from project $projectId")
    return userDao.deleteAllByProject_ProjectId(projectId)
  }

  // Helpers

  @Throws
  private fun fetchUser(userId: UserId, withTokens: Boolean): User {
    val user = userDao.findById(userId.toData()).get().toDomain()
    if (!withTokens) return user
    val project = adminRepository.fetchProjectById(userId.projectId)
    val tokens = ownedTokenRepository.fetchAllTokens(user, project)
    return user.copy(ownedTokens = tokens)
  }

  private fun User.updateVerificationToken(project: Project, oldUser: User? = null): User = when {
    needsNewEmailToken(project, oldUser) -> copy(verificationToken = TokenGenerator.nextEmailToken)
    needsNewPhoneToken(project, oldUser) -> copy(verificationToken = TokenGenerator.nextPhoneToken)
    else -> this
  }

  private fun User.hasEmailIdChanged(project: Project, oldUser: User? = null): Boolean =
    project.userIdType == UserIdType.EMAIL && oldUser?.userId?.id != userId.id

  private fun User.hasPhoneIdChanged(project: Project, oldUser: User? = null): Boolean =
    project.userIdType == UserIdType.PHONE && oldUser?.userId?.id != userId.id

  private fun User.needsNewEmailToken(project: Project, oldUser: User? = null): Boolean {
    // check if contact ID changed
    if (hasEmailIdChanged(project, oldUser)) return true
    // only one verification code exists: if Phone ID changed, assume that Contact Email hasn't
    val hasContactEmailChanged = contactType == ContactType.EMAIL && contact != null && oldUser?.contact != contact
    return hasContactEmailChanged && !hasPhoneIdChanged(project, oldUser)
  }

  private fun User.needsNewPhoneToken(project: Project, oldUser: User? = null): Boolean {
    // check if contact ID changed
    if (hasPhoneIdChanged(project, oldUser)) return true
    // only one verification code exists: if Email ID changed, assume that Contact Phone hasn't
    val hasContactPhoneChanged = contactType == ContactType.PHONE && contact != null && oldUser?.contact != contact
    return hasContactPhoneChanged && !hasEmailIdChanged(project, oldUser)
  }

}