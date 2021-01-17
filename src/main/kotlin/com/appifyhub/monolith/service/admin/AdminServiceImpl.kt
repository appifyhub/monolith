package com.appifyhub.monolith.service.admin

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountCreator
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.service.user.UserService
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
    // TODO MM validation missing
    return adminRepository.addProject(creator)
  }

  override fun addAccount(creator: AccountCreator): Account {
    log.debug("Adding account $creator")
    // TODO MM validation missing
    return adminRepository.addAccount(creator)
  }

  override fun fetchAccountById(id: Long): Account {
    log.debug("Fetching account by id $id")
    // TODO MM validation missing
    return adminRepository.fetchAccountById(id)
  }

  override fun fetchProjectById(id: Long): Project {
    log.debug("Fetching project by id $id")
    // TODO MM validation missing
    return adminRepository.fetchProjectById(id)
  }

  override fun fetchProjectBySignature(signature: String): Project {
    log.debug("Fetching project by signature $signature")
    // TODO MM validation missing
    return adminRepository.fetchProjectBySignature(signature)
  }

  override fun fetchAdminProject(): Project {
    log.debug("Fetching admin project")
    return adminRepository.getAdminProject()
  }

  override fun fetchAllProjectsByAccount(account: Account): List<Project> {
    log.debug("Fetching all projects for account $account")
    // TODO MM validation missing
    return adminRepository.fetchAllProjectsByAccount(account)
  }

  override fun updateProject(updater: ProjectUpdater): Project {
    log.debug("Updating project $updater")
    // TODO MM validation missing
    return adminRepository.updateProject(updater)
  }

  override fun updateAccount(updater: AccountUpdater): Account {
    log.debug("Updating account $updater")
    // TODO MM validation missing
    return adminRepository.updateAccount(updater)
  }

  override fun removeProjectById(projectId: Long) {
    log.debug("Removing project $projectId")
    // TODO MM validation missing
    // cascade manually for now
    userRepository.removeAllUsersByProjectId(projectId)
    return adminRepository.removeProjectById(projectId)
  }

  override fun removeProjectBySignature(signature: String) {
    log.debug("Removing project with signature $signature")
    // TODO MM validation missing
    // cascade manually for now
    val project = adminRepository.fetchProjectBySignature(signature)
    userRepository.removeAllUsersByProjectId(project.id)
    return adminRepository.removeProjectBySignature(signature)
  }

  override fun removeAccountById(accountId: Long) {
    log.debug("Removing account $accountId")
    // TODO MM validation missing
    // cascade manually for now
    val account = adminRepository.fetchAccountById(accountId)
    val projects = adminRepository.fetchAllProjectsByAccount(account)
    projects.forEach { userRepository.removeAllUsersByProjectId(it.id) }
    adminRepository.removeAllProjectsByAccount(account)
    return adminRepository.removeAccountById(accountId)
  }

}