package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater

interface UserService {

  enum class Privilege(val minLevel: Authority) {
    USER_READ(Authority.MODERATOR),
    USER_WRITE(Authority.ADMIN),
    PROJECT_READ(Authority.OWNER),
    PROJECT_WRITE(Authority.OWNER),
  }

  @Throws fun addUser(creator: UserCreator, userIdType: Project.UserIdType): User

  @Throws fun fetchUserByUserId(id: UserId, withTokens: Boolean): User

  @Throws fun fetchUserByUniversalId(universalId: String, withTokens: Boolean): User

  @Throws fun fetchAllUsersByContact(contact: String): List<User>

  @Throws fun fetchAllUsersByProjectId(projectId: Long): List<User>

  @Throws fun fetchAllUsersByAccount(account: Account): List<User>

  @Throws fun updateUser(updater: UserUpdater, userIdType: Project.UserIdType): User

  @Throws fun removeUserById(id: UserId)

  @Throws fun removeUserByUniversalId(universalId: String)

  @Throws fun removeAllUsersByProjectId(projectId: Long)

}
