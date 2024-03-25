package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.common.stubProject
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.mapper.toSecurityUser
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.storage.dao.UserDao
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

// Note: this is a spring component (and possibly not needed at all)

@Primary
@Component
class SpringSecurityUserManagerImpl(
  private val userDao: UserDao,
  private val passwordEncoder: PasswordEncoder,
  private val timeProvider: TimeProvider,
) : SpringSecurityUserManager {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun createUser(user: UserDetails?) {
    log.warn("Security: creating $user")
    val domainUser = user!!.toDomain(timeProvider).let {
      it.updateVerificationToken(
        project = stubProject().copy(id = it.id.projectId)
      )
    }
    userDao.save(domainUser.toData())
  }

  override fun loadUserByUsername(username: String?): UserDetails {
    log.debug("Security: loading $username")
    val id = UserId.fromUniversalFormat(username!!)
    return userDao.findById(id.toData()).get().toDomain().toSecurityUser()
  }

  override fun userExists(username: String?): Boolean {
    log.debug("Security: checking if $username exists")
    return try {
      val id = UserId.fromUniversalFormat(username!!)
      userDao.findById(id.toData()).get().toDomain().let { true }
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
    val fetchedUser = userDao.findById(domainUser.id.toData()).get().toDomain()
    val project = stubProject().copy(id = fetchedUser.id.projectId)
    val updatedUser = fetchedUser
      .updateFromSecurityUser(domainUser.toSecurityUser())
      .updateVerificationToken(project, oldUser = fetchedUser)

    userDao.save(updatedUser.toData())
  }

  override fun deleteUser(username: String?) {
    log.warn("Security: deleting user $username")
    val id = UserId.fromUniversalFormat(username!!)
    userDao.deleteById(id.toData())
  }

  override fun changePassword(oldPassword: String?, newPassword: String?) {
    log.warn("Security: change password")
  }

  override fun updatePassword(user: UserDetails?, newPassword: String?): UserDetails {
    log.warn("Security: updating password $user")
    val domainUser = user!!.toDomain(timeProvider).copy(signature = newPassword!!)
    val fetchedUser = userDao.findById(domainUser.id.toData()).get().toDomain()
    val project = stubProject().copy(id = fetchedUser.id.projectId)
    val updatedUser = fetchedUser
      .updateFromSecurityUser(domainUser.toSecurityUser())
      .updateVerificationToken(project, oldUser = fetchedUser)

    return userDao.save(updatedUser.toData()).toDomain().toSecurityUser()
  }

  // Helpers

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

  private fun User.hasEmailIdChanged(project: Project, oldUser: User? = null): Boolean =
    project.userIdType == Project.UserIdType.EMAIL && oldUser?.id?.userId != id.userId

  private fun User.hasPhoneIdChanged(project: Project, oldUser: User? = null): Boolean =
    project.userIdType == Project.UserIdType.PHONE && oldUser?.id?.userId != id.userId

  private fun User.needsNewEmailToken(project: Project, oldUser: User? = null): Boolean {
    // check if contact ID changed
    if (hasEmailIdChanged(project, oldUser)) return true
    // only one verification code exists: if Phone ID changed, assume that Contact Email hasn't
    val hasContactEmailChanged = contactType == User.ContactType.EMAIL && contact != null && oldUser?.contact != contact
    return hasContactEmailChanged && !hasPhoneIdChanged(project, oldUser)
  }

  private fun User.needsNewPhoneToken(project: Project, oldUser: User? = null): Boolean {
    // check if contact ID changed
    if (hasPhoneIdChanged(project, oldUser)) return true
    // only one verification code exists: if Email ID changed, assume that Contact Phone hasn't
    val hasContactPhoneChanged = contactType == User.ContactType.PHONE && contact != null && oldUser?.contact != contact
    return hasContactPhoneChanged && !hasEmailIdChanged(project, oldUser)
  }

}
