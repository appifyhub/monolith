package com.appifyhub.monolith.repository.admin

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater

typealias RawProject = Project

interface AdminRepository {

  @Throws fun addProject(creator: ProjectCreator): RawProject

  @Throws fun addAccount(): Account

  @Throws fun fetchAccountById(id: Long): Account

  @Throws fun fetchProjectById(id: Long): Project

  @Throws fun fetchProjectBySignature(rawSignature: String): Project

  @Throws fun getAdminProject(): Project

  @Throws fun fetchAllProjectsByAccount(account: Account): List<Project>

  @Throws fun regenerateProjectSignature(id: Long): RawProject

  @Throws fun updateProject(updater: ProjectUpdater): Project

  @Throws fun updateAccount(updater: AccountUpdater): Account

  @Throws fun removeProjectById(projectId: Long)

  @Throws fun removeProjectBySignature(rawSignature: String)

  @Throws fun removeAllProjectsByAccount(account: Account)

  @Throws fun removeAccountById(accountId: Long)

}