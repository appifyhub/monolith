package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
  private val userRepository: UserRepository,
) : UserService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addUser(creator: UserCreator, project: Project): User {
    log.debug("Adding user $creator")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.addUser(creator, project)
  }

  override fun fetchUserByUserId(userId: UserId, withTokens: Boolean): User {
    log.debug("Fetching user by $userId")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.fetchUserByUserId(userId, withTokens = withTokens)
  }

  override fun fetchUserByUnifiedIdFormat(idHashProjectId: String, withTokens: Boolean): User {
    log.debug("Fetching user by $idHashProjectId")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.fetchUserByUnifiedIdFormat(idHashProjectId, withTokens = withTokens)
  }

  override fun fetchAllUsersByContact(contact: String): List<User> {
    log.debug("Fetching user by $contact")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.fetchAllUsersByContact(contact)
  }

  override fun fetchAllUsersByProjectId(projectId: Long): List<User> {
    log.debug("Fetching all users by project $projectId")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.fetchAllUsersByProjectId(projectId)
  }

  override fun fetchAllUsersByAccount(account: Account): List<User> {
    log.debug("Fetching all users for account $account")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.fetchAllUsersByAccount(account)
  }

  override fun updateUser(updater: UserUpdater, project: Project): User {
    log.debug("Updating user by $updater")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.updateUser(updater, project)
  }

  override fun removeUserById(userId: UserId) {
    log.debug("Removing user by $userId")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.removeUserById(userId)
  }

  override fun removeUserByUnifiedFormat(idHashProjectId: String) {
    log.debug("Removing user by $idHashProjectId")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.removeUserByUnifiedFormat(idHashProjectId)
  }

  override fun removeAllUsersByProjectId(projectId: Long) {
    log.debug("Removing all users from project $projectId")
    // TODO MM validation missing + check blocked tokens and expiration?
    return userRepository.removeAllUsersByProjectId(projectId)
  }

}