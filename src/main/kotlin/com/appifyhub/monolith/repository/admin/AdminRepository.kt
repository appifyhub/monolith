package com.appifyhub.monolith.repository.admin

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountOwnerUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater

interface AdminRepository {

  @Throws fun addProject(creator: ProjectCreator): Project

  @Throws fun addAccount(): Account

  @Throws fun fetchAccountById(id: Long): Account

  @Throws fun fetchProjectById(id: Long): Project

  @Throws fun getAdminProject(): Project

  @Throws fun fetchAllProjectsByAccount(account: Account): List<Project>

  @Throws fun updateProject(updater: ProjectUpdater): Project

  @Throws fun updateAccount(updater: AccountOwnerUpdater): Account

  @Throws fun removeProjectById(projectId: Long)

  @Throws fun removeAllProjectsByAccount(account: Account)

  @Throws fun removeAccountById(accountId: Long)

}
