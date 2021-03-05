package com.appifyhub.monolith.repository.admin

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import com.appifyhub.monolith.domain.admin.ops.AccountUpdater
import com.appifyhub.monolith.domain.admin.ops.ProjectUpdater
import com.appifyhub.monolith.storage.dao.AccountDao
import com.appifyhub.monolith.storage.dao.ProjectDao
import com.appifyhub.monolith.storage.model.admin.AccountDbm
import com.appifyhub.monolith.storage.model.admin.ProjectDbm
import com.appifyhub.monolith.util.PasswordEncoderFake
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.Optional

class AdminRepositoryImplTest {

  private val accountDao = mock<AccountDao>()
  private val projectDao = mock<ProjectDao>()
  private val passwordEncoder = PasswordEncoderFake()
  private val timeProvider = TimeProviderFake()

  private val repository = AdminRepositoryImpl(
    accountDao = accountDao,
    projectDao = projectDao,
    passwordEncoder = passwordEncoder,
    timeProvider = timeProvider,
  )

  @BeforeEach fun setup() {
    projectDao.stub {
      onGeneric { findAll() } doReturn listOf(Stubs.projectDbm)
      onGeneric { save(any()) } doAnswer {
        (it.arguments.first() as ProjectDbm).apply {
          projectId = Stubs.project.id
        }
      }
    }
    accountDao.stub {
      onGeneric { save(any()) } doAnswer {
        (it.arguments.first() as AccountDbm).apply {
          accountId = Stubs.account.id
        }
      }
    }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
    SignatureGenerator.interceptor = { null }
  }

  // Creating

  @Test fun `adding a project generates a signature`() {
    // create & update times from the stub
    val timesIterator = listOf(Date(0xC20000), Date(0xA20000)).iterator()
    timeProvider.staticTime = { timesIterator.next().time }
    SignatureGenerator.interceptor = { "signature" }

    assertThat(repository.addProject(Stubs.projectCreator))
      .isDataClassEqualTo(
        Stubs.project.copy(
          account = Stubs.account.copy(owners = emptyList()), // not available in admin DAOs
          signature = "signature", // creation returns a RawProject
        )
      )
  }

  @Test fun `adding an account works`() {
    // create & update times from the stub
    val timesIterator = listOf(Date(0xC10000), Date(0xA10000)).iterator()
    timeProvider.staticTime = { timesIterator.next().time }

    assertThat(repository.addAccount())
      .isDataClassEqualTo(
        Stubs.account.copy(
          owners = emptyList(), // not available for new accounts
        )
      )
  }

  // Getting

  @Test fun `fetching account by ID works`() {
    accountDao.stub {
      onGeneric { findById(Stubs.account.id) } doReturn Optional.of(Stubs.accountDbm)
    }

    assertThat(repository.fetchAccountById(Stubs.account.id))
      .isDataClassEqualTo(
        Stubs.account.copy(
          owners = emptyList(), // not available in admin DAOs
        )
      )
  }

  @Test fun `fetching admin project works`() {
    assertThat(repository.getAdminProject())
      .isDataClassEqualTo(
        Stubs.project.copy(
          account = Stubs.account.copy(owners = emptyList()), // not available in admin DAOs
        )
      )
  }

  @Test fun `fetching project by ID works`() {
    projectDao.stub {
      onGeneric { findById(Stubs.project.id) } doReturn Optional.of(Stubs.projectDbm)
    }

    assertThat(repository.fetchProjectById(Stubs.project.id))
      .isDataClassEqualTo(
        Stubs.project.copy(
          account = Stubs.account.copy(owners = emptyList()), // not available in admin DAOs
        )
      )
  }

  @Test fun `fetching project by signature works`() {
    projectDao.stub {
      onGeneric { findBySignature("signature") } doReturn Optional.of(Stubs.projectDbm)
    }

    assertThat(repository.fetchProjectBySignature(Stubs.project.signature))
      .isDataClassEqualTo(
        Stubs.project.copy(
          account = Stubs.account.copy(owners = emptyList()), // not available in admin DAOs
        )
      )
  }

  @Test fun `fetching all projects by account works`() {
    projectDao.stub {
      onGeneric { findAllByAccount(Stubs.accountDbm) } doReturn listOf(Stubs.projectDbm)
    }

    assertThat(repository.fetchAllProjectsByAccount(Stubs.account))
      .isEqualTo(
        listOf(
          Stubs.project.copy(
            account = Stubs.account.copy(owners = emptyList()), // not available in admin DAOs
          )
        )
      )
  }

  // Updating

  @Test fun `regenerating project signature works`() {
    projectDao.stub {
      onGeneric { findById(Stubs.project.id) } doReturn Optional.of(Stubs.projectDbm)
    }
    timeProvider.staticTime = { 0xA20001 }
    SignatureGenerator.interceptor = { "signature1" }

    assertThat(repository.regenerateProjectSignature(Stubs.project.id))
      .isDataClassEqualTo(
        Stubs.project.copy(
          account = Stubs.account.copy(owners = emptyList()), // not available in admin DAOs
          signature = "signature1", // regeneration returns a RawProject
          updatedAt = Date(0xA20001),
        )
      )
  }

  @Test fun `updating project with no changes changes nothing`() {
    projectDao.stub {
      onGeneric { findById(Stubs.project.id) } doReturn Optional.of(Stubs.projectDbm)
    }
    timeProvider.staticTime = { 0xA20001 }

    val emptyUpdater = ProjectUpdater(id = Stubs.project.id)
    assertThat(repository.updateProject(emptyUpdater))
      .isDataClassEqualTo(
        Stubs.project.copy(
          account = Stubs.account.copy(owners = emptyList()), // not available in admin DAOs
          updatedAt = Date(0xA20001),
        )
      )
  }

  @Test fun `updating project works`() {
    projectDao.stub {
      onGeneric { findById(Stubs.project.id) } doReturn Optional.of(Stubs.projectDbm)
    }
    timeProvider.staticTime = { 0xA20001 } // from the stub

    assertThat(repository.updateProject(Stubs.projectUpdater))
      .isDataClassEqualTo(
        Stubs.projectUpdated.copy(
          account = Stubs.accountUpdated.copy(owners = emptyList()), // not available in admin DAOs
        )
      )
  }

  @Test fun `updating account with no changes changes nothing`() {
    accountDao.stub {
      onGeneric { findById(Stubs.account.id) } doReturn Optional.of(Stubs.accountDbm)
    }
    timeProvider.staticTime = { 0xA10001 }

    val emptyUpdater = AccountUpdater(id = Stubs.account.id)
    assertThat(repository.updateAccount(emptyUpdater))
      .isDataClassEqualTo(
        Stubs.account.copy(
          owners = emptyList(), // not available in admin DAOs
          updatedAt = Date(0xA10001),
        )
      )
  }

  @Test fun `updating account works`() {
    accountDao.stub {
      onGeneric { findById(Stubs.account.id) } doReturn Optional.of(Stubs.accountDbm)
    }
    timeProvider.staticTime = { 0xA10001 } // from the stub

    assertThat(repository.updateAccount(Stubs.accountUpdater))
      .isDataClassEqualTo(
        Stubs.accountUpdated.copy(
          owners = emptyList(), // not available in admin DAOs
        )
      )
  }

  // Deleting

  @Test fun `removing project by ID works`() {
    projectDao.stub {
      onGeneric { deleteById(Stubs.project.id) } doAnswer { }
    }

    assertThat { repository.removeProjectById(Stubs.project.id) }
      .isSuccess()
  }

  @Test fun `removing project by signature works`() {
    projectDao.stub {
      onGeneric { deleteBySignature(Stubs.project.signature) } doAnswer { }
    }

    assertThat { repository.removeProjectBySignature("signature") }
      .isSuccess()
  }

  @Test fun `removing all projects by account works`() {
    projectDao.stub {
      onGeneric { deleteAllByAccount(Stubs.accountDbm) } doAnswer { }
    }

    assertThat { repository.removeAllProjectsByAccount(Stubs.account) }
      .isSuccess()
  }

  @Test fun `removing account by ID works`() {
    accountDao.stub {
      onGeneric { deleteById(Stubs.account.id) } doAnswer { }
    }

    assertThat { repository.removeAccountById(Stubs.account.id) }
      .isSuccess()
  }

}