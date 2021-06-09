package com.appifyhub.monolith.service.admin

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.common.mapValueNonNull
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AdminServiceImpl(
  private val adminRepository: AdminRepository,
  private val userRepository: UserService,
) : AdminService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addProject(creator: ProjectCreator): Project {
    log.debug("Adding project $creator")

    val normalizeAccount = Normalizers.AccountId.run(creator.account.id).requireValid { "Account ID" }
      .let { creator.account.copy(id = it) }
    val normalizedName = Normalizers.PropProjectName.run(creator.name).requireValid { "Project Name" }

    val normalizedCreator = ProjectCreator(
      account = normalizeAccount,
      name = normalizedName,
      type = creator.type,
      status = creator.status,
      userIdType = creator.userIdType,
    )

    return adminRepository.addProject(normalizedCreator)
  }

  override fun addAccount(): Account {
    log.debug("Adding account")
    return adminRepository.addAccount()
  }

  override fun getAdminProject(): Project {
    log.debug("Fetching admin project")
    return adminRepository.getAdminProject()
  }

  override fun fetchAccountById(id: Long): Account {
    log.debug("Fetching account by id $id")
    val normalizedAccountId = Normalizers.AccountId.run(id).requireValid { "Account ID" }
    return adminRepository.fetchAccountById(normalizedAccountId)
  }

  override fun fetchProjectById(id: Long): Project {
    log.debug("Fetching project by id $id")
    val normalizedProjectId = Normalizers.ProjectId.run(id).requireValid { "Project ID" }
    return adminRepository.fetchProjectById(normalizedProjectId)
  }

  override fun fetchAllProjectsByAccount(account: Account): List<Project> {
    log.debug("Fetching all projects for account $account")
    val normalizedAccount = Normalizers.AccountId.run(account.id).requireValid { "Account ID" }
      .let { account.copy(id = it) }
    return adminRepository.fetchAllProjectsByAccount(normalizedAccount)
  }

  override fun updateProject(updater: ProjectUpdater): Project {
    log.debug("Updating project $updater")

    val normalizedProjectId = Normalizers.ProjectId.run(updater.id).requireValid { "Project ID" }
    updater.account?.value?.let {
      Normalizers.AccountId.run(it.id).requireValid { "Account ID" }
    }
    val normalizedName = updater.name?.mapValueNonNull {
      Normalizers.PropProjectName.run(it).requireValid { "Project Name" }
    }

    val normalizedUpdater = ProjectUpdater(
      id = normalizedProjectId,
      account = updater.account,
      name = normalizedName,
      type = updater.type,
      status = updater.status,
    )

    return adminRepository.updateProject(normalizedUpdater)
  }

  override fun updateAccount(updater: AccountUpdater): Account {
    log.debug("Updating account $updater")

    Normalizers.AccountId.run(updater.id).requireValid { "Account ID" }
    updater.addedOwners?.value?.forEach { Normalizers.UserId.run(it.id).requireValid { "Added User ID" } }
    updater.removedOwners?.value?.forEach { Normalizers.UserId.run(it.id).requireValid { "Removed User ID" } }

    return adminRepository.updateAccount(updater)
  }

  override fun removeProjectById(projectId: Long) {
    log.debug("Removing project $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    // cascade manually for now
    userRepository.removeAllUsersByProjectId(normalizedProjectId)
    return adminRepository.removeProjectById(normalizedProjectId)
  }

  override fun removeAccountById(accountId: Long) {
    log.debug("Removing account $accountId")

    val normalizedAccountId = Normalizers.AccountId.run(accountId).requireValid { "Account ID" }

    // cascade manually for now
    val account = adminRepository.fetchAccountById(normalizedAccountId)
    val projects = adminRepository.fetchAllProjectsByAccount(account)
    projects.forEach { userRepository.removeAllUsersByProjectId(it.id) }
    adminRepository.removeAllProjectsByAccount(account)
    return adminRepository.removeAccountById(account.id)
  }

}
