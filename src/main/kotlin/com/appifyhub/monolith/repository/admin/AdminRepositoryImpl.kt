package com.appifyhub.monolith.repository.admin

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.ops.AccountCreator
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectCreator
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.repository.mapper.applyTo
import com.appifyhub.monolith.repository.mapper.toAccountData
import com.appifyhub.monolith.repository.mapper.toData
import com.appifyhub.monolith.repository.mapper.toDomain
import com.appifyhub.monolith.repository.mapper.toProjectData
import com.appifyhub.monolith.storage.dao.AccountDao
import com.appifyhub.monolith.storage.dao.ProjectDao
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository

@Repository
class AdminRepositoryImpl(
  private val accountDao: AccountDao,
  private val projectDao: ProjectDao,
  private val timeProvider: TimeProvider,
  private val passwordEncoder: PasswordEncoder,
) : AdminRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  private var adminProject = projectDao.findAll()
    .map { it.toDomain() }
    .minByOrNull { it.id }!!

  override fun addProject(creator: ProjectCreator): Project {
    log.debug("Adding project by creator $creator")
    val projectData = creator.toProjectData(
      signature = SignatureGenerator.nextSignature,
      timeProvider = timeProvider,
    )
    return projectDao.save(projectData).toDomain()
  }

  override fun addAccount(creator: AccountCreator): Account {
    log.debug("Adding account by creator $creator")
    val accountData = creator.toAccountData(timeProvider = timeProvider)
    return accountDao.save(accountData).toDomain()
  }

  override fun fetchAccountById(id: Long): Account {
    log.debug("Fetching account by id $id")
    return accountDao.findById(id).get().toDomain()
  }

  override fun fetchAdminProject(): Project {
    log.debug("Fetching admin project")
    return adminProject
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
      timeProvider = timeProvider,
      passwordEncoder = passwordEncoder
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