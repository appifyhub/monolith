package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater

interface UserService {

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
