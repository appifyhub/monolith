package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.User.IdType
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.mapper.applyTo
import com.appifyhub.monolith.repository.mapper.toData
import com.appifyhub.monolith.repository.mapper.toDomain
import com.appifyhub.monolith.repository.mapper.toSecurityUser
import com.appifyhub.monolith.repository.mapper.toUser
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
) : UserRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  // region Domain-level

  override fun addUser(creator: UserCreator): User {
    log.debug("Adding user by creator $creator")
    if (creator.id == null && creator.idType != IdType.RANDOM)
      throw IllegalArgumentException("Missing user ID for creator $creator")

    val user = creator.toUser(
      userId = creator.id ?: UserIdGenerator.nextId,
      timeProvider = timeProvider,
      passwordEncoder = passwordEncoder,
    ).updateVerificationToken()

    return userDao.save(user.toData()).toDomain()
  }

  override fun fetchUserByUserId(userId: UserId): User {
    log.debug("Fetching user $userId")
    return userDao.findById(userId.toData()).get().toDomain()
  }

  override fun fetchUserByUnifiedIdFormat(idHashProjectId: String): User {
    log.debug("Fetching user $idHashProjectId")
    val userId = UserId.fromUnifiedFormat(idHashProjectId)
    return userDao.findById(userId.toData()).get().toDomain()
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

  override fun updateUser(updater: UserUpdater): User {
    log.debug("Updating user $updater")
    val fetchedUser = fetchUserByUserId(updater.id)
    val updatedUser = updater.applyTo(
      user = fetchedUser,
      timeProvider = timeProvider,
      passwordEncoder = passwordEncoder,
    ).updateVerificationToken(oldUser = fetchedUser)

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
    val domainUser = user!!.toDomain(timeProvider).updateVerificationToken()
    userDao.save(domainUser.toData())
  }

  override fun loadUserByUsername(username: String?): UserDetails {
    log.debug("Security: loading $username")
    val id = UserId.fromUnifiedFormat(username!!)
    return fetchUserByUserId(id).toSecurityUser()
  }

  override fun userExists(username: String?): Boolean {
    log.debug("Security: checking if $username exists")
    return try {
      val id = UserId.fromUnifiedFormat(username!!)
      fetchUserByUserId(id).let { true }
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
    val fetchedUser = fetchUserByUserId(domainUser.userId)
    val updatedUser = fetchedUser
      .updateFromSecurityUser(domainUser.toSecurityUser())
      .updateVerificationToken(oldUser = fetchedUser)

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
    val fetchedUser = fetchUserByUserId(domainUser.userId)
    val updatedUser = fetchedUser
      .updateFromSecurityUser(domainUser.toSecurityUser())
      .updateVerificationToken(oldUser = fetchedUser)

    return userDao.save(updatedUser.toData()).toDomain().toSecurityUser()
  }

  // endregion

  // region Helpers

  private fun User.updateFromSecurityUser(user: UserDetails): User {
    val signatureChanged = user.password.isNotBlank() && user.password != signature
    return copy(
      signature = user.password.let { if (signatureChanged) passwordEncoder.encode(it) else it },
      authority = user.toDomain(timeProvider).authority,
      updatedAt = timeProvider.currentDate,
    )
  }

  private fun User.updateVerificationToken(oldUser: User? = null): User = when {
    needsNewEmailToken(oldUser) -> copy(verificationToken = TokenGenerator.nextEmailToken)
    needsNewPhoneToken(oldUser) -> copy(verificationToken = TokenGenerator.nextPhoneToken)
    else -> this
  }

  private fun User.needsNewEmailToken(oldUser: User? = null): Boolean {
    val hasEmailChanged = idType == IdType.EMAIL && oldUser?.userId?.id != userId.id
    val hasContactEmailChanged = contactType == ContactType.EMAIL && contact != null && oldUser?.contact != contact
    return hasEmailChanged || hasContactEmailChanged
  }

  private fun User.needsNewPhoneToken(oldUser: User? = null): Boolean {
    val hasPhoneChanged = idType == IdType.PHONE && oldUser?.userId?.id != userId.id
    val hasContactPhoneChanged = contactType == ContactType.PHONE && contact != null && oldUser?.contact != contact
    return hasPhoneChanged || hasContactPhoneChanged
  }

  // endregion

}