package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater

interface UserRepository : SpringSecurityUserManager {

  @Throws fun addUser(creator: UserCreator, project: Project): User

  @Throws fun fetchUserByUserId(userId: UserId, withTokens: Boolean): User

  @Throws fun fetchUserByUnifiedIdFormat(idHashProjectId: String, withTokens: Boolean): User

  @Throws fun fetchAllUsersByContact(contact: String): List<User>

  @Throws fun fetchAllUsersByProjectId(projectId: Long): List<User>

  @Throws fun fetchAllUsersByAccount(account: Account): List<User>

  @Throws fun updateUser(updater: UserUpdater, project: Project): User

  @Throws fun removeUserById(userId: UserId)

  @Throws fun removeUserByUnifiedFormat(idHashProjectId: String)

  @Throws fun removeAllUsersByProjectId(projectId: Long)

}