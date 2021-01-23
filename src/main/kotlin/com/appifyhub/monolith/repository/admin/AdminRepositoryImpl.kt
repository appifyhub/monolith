package com.appifyhub.monolith.repository.admin

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.domain.mapper.applyTo
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.mapper.toProjectData
import com.appifyhub.monolith.storage.dao.AccountDao
import com.appifyhub.monolith.storage.dao.ProjectDao
import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository

@Repository
class AdminRepositoryImpl(
  private val accountDao: AccountDao,
  private val projectDao: ProjectDao,
  private val passwordEncoder: PasswordEncoder,
  private val timeProvider: TimeProvider,
) : AdminRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  private val lazyAdminProject: Project by lazy {
    projectDao.findAll()
      .map { it.toDomain() }
      .minByOrNull { it.id }!!
  }

  override fun addProject(creator: ProjectCreator): Project {
    log.debug("Adding project by creator $creator")
    val projectData = creator.toProjectData(
      signature = SignatureGenerator.nextSignature,
      timeProvider = timeProvider,
    )
    return projectDao.save(projectData).toDomain()
  }

  override fun addAccount(): Account {
    log.debug("Adding account by creator")

    val accountData = AccountDbm(
      accountId = null,
      createdAt = timeProvider.currentDate,
      updatedAt = timeProvider.currentDate,
    )
    return accountDao.save(accountData).toDomain()
  }

  override fun fetchAccountById(id: Long): Account {
    log.debug("Fetching account by id $id")
    return accountDao.findById(id).get().toDomain()
  }

  override fun getAdminProject(): Project {
    log.debug("Fetching admin project")
    return lazyAdminProject
  }

  override fun fetchProjectById(id: Long): Project {
    log.debug("Fetching project by id $id")
    return projectDao.findById(id).get().toDomain()
  }

  override fun fetchProjectBySignature(signature: String): Project {
    log.debug("Fetching project by signature $signature")
    return projectDao.findBySignature(signature).get().toDomain()
  }

  override fun fetchAllProjectsByAccount(account: Account): List<Project> {
    log.debug("Fetching all projects by account $account")
    return projectDao.findAllByAccount(account.toData()).map(ProjectDbm::toDomain)
  }

  override fun updateProject(updater: ProjectUpdater): Project {
    log.debug("Updating project $updater")
    val fetchedProject = fetchProjectById(updater.id)
    val updatedProject = updater.applyTo(
      project = fetchedProject,
      passwordEncoder = passwordEncoder,
      timeProvider = timeProvider,
    )
    return projectDao.save(updatedProject.toData()).toDomain()
  }

  override fun updateAccount(updater: AccountUpdater): Account {
    log.debug("Updating account $updater")
    val fetchedAccount = fetchAccountById(updater.id)
    val updatedAccount = updater.applyTo(
      account = fetchedAccount,
      timeProvider = timeProvider,
    )
    return accountDao.save(updatedAccount.toData()).toDomain()
  }

  override fun removeProjectById(projectId: Long) {
    log.debug("Removing project $projectId")
    projectDao.deleteById(projectId)
  }

  override fun removeProjectBySignature(signature: String) {
    log.debug("Removing project with signature $signature")
    projectDao.deleteBySignature(signature)
  }

  override fun removeAllProjectsByAccount(account: Account) {
    log.debug("Removing all projects for account $account")
    projectDao.deleteAllByAccount(account.toData())
  }

  override fun removeAccountById(accountId: Long) {
    log.debug("Removing account $accountId")
    accountDao.deleteById(accountId)
  }
}