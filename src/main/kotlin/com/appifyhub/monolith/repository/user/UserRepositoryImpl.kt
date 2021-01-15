package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.Project.UserIdType
import com.appifyhub.monolith.domain.common.stubProject
import com.appifyhub.monolith.domain.mapper.applyTo
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.mapper.toSecurityUser
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
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
  private val timeProvider: TimeProvider,
  private val userDao: UserDao,
  private val passwordEncoder: PasswordEncoder,
  private val ownedTokenRepository: OwnedTokenRepository,
  private val adminRepository: AdminRepository,
) : UserRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  // region Domain-level

  override fun addUser(creator: UserCreator, project: Project): User {
    log.debug("Adding user by creator $creator")
    if (creator.id == null && project.userIdType != UserIdType.RANDOM)
      throw IllegalArgumentException("Missing user ID for creator $creator")

    val user = creator.toUser(
      userId = creator.id ?: UserIdGenerator.nextId,
      timeProvider = timeProvider,
      passwordEncoder = passwordEncoder,
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
      timeProvider = timeProvider,
      passwordEncoder = passwordEncoder,
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

  // endregion

  // region Spring Security

  override fun createUser(user: UserDetails?) {
    log.warn("Security: creating $user")
    val domainUser = user!!.toDomain(timeProvider).let {
      it.updateVerificationToken(
        project = stubProject().copy(id = it.userId.projectId)
      )
    }
    userDao.save(domainUser.toData())
  }

  override fun loadUserByUsername(username: String?): UserDetails {
    log.debug("Security: loading $username")
    val id = UserId.fromUnifiedFormat(username!!)
    return fetchUser(id, withTokens = false).toSecurityUser()
  }

  override fun userExists(username: String?): Boolean {
    log.debug("Security: checking if $username exists")
    return try {
      val id = UserId.fromUnifiedFormat(username!!)
      fetchUser(id, withTokens = false).let { true }
    } catch (t: Throwable) {
      log.warn("Couldn't check if $username exists", t)
      false
    }
  }

  override fun check(toCheck: UserDetails?) {
    log.warn("Security: checking user $toCheck")
  }

  override fun updateUser(user: UserDetails?) {
    log.warn("Security: updating user $user")
    val domainUser = user!!.toDomain(timeProvider)
    val fetchedUser = fetchUser(domainUser.userId, withTokens = false)
    val project = stubProject().copy(id = fetchedUser.userId.projectId)
    val updatedUser = fetchedUser
      .updateFromSecurityUser(domainUser.toSecurityUser())
      .updateVerificationToken(project, oldUser = fetchedUser)

    userDao.save(updatedUser.toData())
  }

  override fun deleteUser(username: String?) {
    log.warn("Security: deleting user $username")
    val id = UserId.fromUnifiedFormat(username!!)
    removeUserById(id)
  }

  override fun changePassword(oldPassword: String?, newPassword: String?) {
    log.warn("Security: change password")
  }

  override fun updatePassword(user: UserDetails?, newPassword: String?): UserDetails {
    log.warn("Security: updating password $user")
    val domainUser = user!!.toDomain(timeProvider).copy(signature = newPassword!!)
    val fetchedUser = fetchUser(domainUser.userId, withTokens = false)
    val project = stubProject().copy(id = fetchedUser.userId.projectId)
    val updatedUser = fetchedUser
      .updateFromSecurityUser(domainUser.toSecurityUser())
      .updateVerificationToken(project, oldUser = fetchedUser)

    return userDao.save(updatedUser.toData()).toDomain().toSecurityUser()
  }

  // endregion

  // region Helpers

  @Throws
  private fun fetchUser(userId: UserId, withTokens: Boolean): User {
    val user = userDao.findById(userId.toData()).get().toDomain()
    if (!withTokens) return user
    val project = adminRepository.fetchProjectById(userId.projectId)
    val tokens = ownedTokenRepository.fetchAllTokens(user, project)
    return user.copy(ownedTokens = tokens)
  }

  private fun User.updateFromSecurityUser(user: UserDetails): User {
    val signatureChanged = user.password.isNotBlank() && user.password != signature
    return copy(
      signature = user.password.let { if (signatureChanged) passwordEncoder.encode(it) else it },
      authority = user.toDomain(timeProvider).authority,
      updatedAt = timeProvider.currentDate,
    )
  }

  private fun User.updateVerificationToken(project: Project, oldUser: User? = null): User = when {
    needsNewEmailToken(project, oldUser) -> copy(verificationToken = TokenGenerator.nextEmailToken)
    needsNewPhoneToken(project, oldUser) -> copy(verificationToken = TokenGenerator.nextPhoneToken)
    else -> this
  }

  private fun User.needsNewEmailToken(project: Project, oldUser: User? = null): Boolean {
    val hasEmailChanged = project.userIdType == UserIdType.EMAIL && oldUser?.userId?.id != userId.id
    val hasContactEmailChanged = contactType == ContactType.EMAIL && contact != null && oldUser?.contact != contact
    return hasEmailChanged || hasContactEmailChanged
  }

  private fun User.needsNewPhoneToken(project: Project, oldUser: User? = null): Boolean {
    val hasPhoneChanged = project.userIdType == UserIdType.PHONE && oldUser?.userId?.id != userId.id
    val hasContactPhoneChanged = contactType == ContactType.PHONE && contact != null && oldUser?.contact != contact
    return hasPhoneChanged || hasContactPhoneChanged
  }

  // endregion

}