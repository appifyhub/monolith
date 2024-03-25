package com.appifyhub.monolith.features.user.domain.service

import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import org.springframework.transaction.annotation.Transactional

interface UserService {

  @Transactional(rollbackFor = [Exception::class])
  @Throws fun addUser(creator: UserCreator): User

  @Throws fun fetchUserByUserId(id: UserId): User

  @Throws fun fetchUserByUniversalId(universalId: String): User

  @Throws fun fetchAllUsersByProjectId(projectId: Long): List<User>

  @Throws fun fetchUserByUserIdAndVerificationToken(userId: UserId, verificationToken: String): User

  @Throws fun searchByName(projectId: Long, name: String): List<User>

  @Throws fun searchByContact(projectId: Long, contact: String): List<User>

  @Throws fun updateUser(updater: UserUpdater): User

  @Throws fun resetSignatureById(id: UserId): User

  @Throws fun removeUserById(id: UserId)

  @Throws fun removeUserByUniversalId(universalId: String)

  @Throws fun removeAllUsersByProjectId(projectId: Long)

}
