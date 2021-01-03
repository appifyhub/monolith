package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater

interface UserService {

  @Throws fun addUser(creator: UserCreator): User

  @Throws fun fetchUserByUserId(userId: UserId): User

  @Throws fun fetchUserByUnifiedIdFormat(idHashProjectId: String): User

  @Throws fun fetchAllUsersByContact(contact: String): List<User>

  @Throws fun fetchAllUsersByProjectId(projectId: Long): List<User>

  @Throws fun fetchAllUsersByAccount(account: Account): List<User>

  @Throws fun updateUser(updater: UserUpdater): User

  @Throws fun removeUserById(userId: UserId)

  @Throws fun removeUserByUnifiedFormat(idHashProjectId: String)

  @Throws fun removeAllUsersByProjectId(projectId: Long)

}