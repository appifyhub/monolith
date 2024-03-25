package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater

interface UserRepository : SpringSecurityUserManager {

  @Throws fun addUser(creator: UserCreator, userIdType: Project.UserIdType): User

  @Throws fun fetchUserByUserId(id: UserId): User

  @Throws fun fetchUserByUniversalId(universalId: String): User

  @Throws fun fetchAllUsersByProjectId(projectId: Long): List<User>

  @Throws fun fetchUserByUserIdAndVerificationToken(userId: UserId, verificationToken: String): User

  @Throws fun searchByName(projectId: Long, name: String): List<User>

  @Throws fun searchByContact(projectId: Long, contact: String): List<User>

  @Throws fun countUsers(projectId: Long): Long

  @Throws fun updateUser(updater: UserUpdater, userIdType: Project.UserIdType): User

  @Throws fun removeUserById(id: UserId)

  @Throws fun removeUserByUniversalId(universalId: String)

  @Throws fun removeAllUsersByProjectId(projectId: Long)

}
