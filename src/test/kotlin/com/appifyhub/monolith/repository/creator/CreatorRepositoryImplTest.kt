package com.appifyhub.monolith.repository.creator

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import com.appifyhub.monolith.domain.creator.ops.ProjectUpdater
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.storage.dao.ProjectCreationDao
import com.appifyhub.monolith.storage.dao.ProjectDao
import com.appifyhub.monolith.storage.model.creator.ProjectDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import java.util.Date
import java.util.Optional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreatorRepositoryImplTest {

  private val projectDao = mock<ProjectDao>()
  private val creationDao = mock<ProjectCreationDao>()
  private val userRepository = mock<UserRepository>()
  private val timeProvider = TimeProviderFake()

  private val repository = CreatorRepositoryImpl(
    projectDao = projectDao,
    creationDao = creationDao,
    userRepository = userRepository,
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
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
    SignatureGenerator.interceptor = { null }
  }

  // Creating

  @Test fun `adding a project works (without creator)`() {
    // create & update times come from the stub
    val timesIterator = listOf(Stubs.project.createdAt, Stubs.project.updatedAt).iterator()
    timeProvider.staticTime = { timesIterator.next().time }
    SignatureGenerator.interceptor = { "signature" }

    verifyZeroInteractions(creationDao)
    assertThat(repository.addProject(Stubs.projectCreator, null))
      .isDataClassEqualTo(Stubs.project)
  }

  @Test fun `adding a project works (with creator)`() {
    // create & update times come from the stub
    val timesIterator = listOf(Stubs.project.createdAt, Stubs.project.updatedAt).iterator()
    val creator = Stubs.user.copy(id = UserId(userId = "uid", projectId = 100))
    timeProvider.staticTime = { timesIterator.next().time }
    SignatureGenerator.interceptor = { "signature" }

    assertThat(repository.addProject(Stubs.projectCreator, creator))
      .isDataClassEqualTo(Stubs.project)
    verify(creationDao).save(anyVararg())
  }

  // Getting

  @Test fun `getting creator project works`() {
    assertThat(repository.getCreatorProject())
      .isDataClassEqualTo(Stubs.project)
  }

  @Test fun `getting creator owner works`() {
    userRepository.stub {
      onGeneric { fetchAllUsersByProjectId(Stubs.project.id) } doReturn listOf(Stubs.user.copy(authority = OWNER))
    }

    assertThat(repository.getCreatorOwner())
      .isDataClassEqualTo(Stubs.user.copy(authority = OWNER))
  }

  @Test fun `fetching project by ID works`() {
    projectDao.stub {
      onGeneric { findById(Stubs.project.id) } doReturn Optional.of(Stubs.projectDbm)
    }

    assertThat(repository.fetchProjectById(Stubs.project.id))
      .isDataClassEqualTo(Stubs.project)
  }

  @Test fun `fetching all projects by creator user ID works`() {
    creationDao.stub {
      onGeneric {
        findAllByData_CreatorUserIdAndData_CreatorProjectId(
          userId = Stubs.userId.userId,
          projectId = Stubs.userId.projectId
        )
      } doReturn listOf(Stubs.projectCreationDbm)
    }
    projectDao.stub {
      onGeneric { findAllById(listOf(Stubs.project.id)) } doReturn listOf(Stubs.projectDbm)
    }

    assertThat(repository.fetchAllProjectsByCreatorUserId(Stubs.userId))
      .isEqualTo(listOf(Stubs.project))
  }

  @Test fun `fetching project creator by project ID works`() {
    creationDao.stub {
      onGeneric { findByData_CreatedProjectId(Stubs.project.id) } doReturn Stubs.projectCreationDbm
    }

    // strip tokens for comparison as they're fetched lazily
    assertThat(repository.fetchProjectCreator(Stubs.project.id))
      .isDataClassEqualTo(Stubs.user)
  }

  // Updating

  @Test fun `updating project with no changes changes nothing`() {
    projectDao.stub {
      onGeneric { findById(Stubs.project.id) } doReturn Optional.of(Stubs.projectDbm)
    }
    timeProvider.staticTime = { 0xA20001 }

    val emptyUpdater = ProjectUpdater(id = Stubs.project.id)
    assertThat(repository.updateProject(emptyUpdater))
      .isDataClassEqualTo(
        Stubs.project.copy(
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
      .isDataClassEqualTo(Stubs.projectUpdated)
  }

  // Deleting

  @Test fun `removing project by ID works`() {
    projectDao.stub {
      onGeneric { deleteById(Stubs.project.id) } doAnswer { }
    }

    assertThat { repository.removeProjectById(Stubs.project.id) }.isSuccess()
    verify(userRepository).removeAllUsersByProjectId(Stubs.project.id)
    verify(creationDao).deleteAllByData_CreatedProjectId(Stubs.project.id)
  }

  @Test fun `removing all projects by creator works`() {
    projectDao.stub {
      onGeneric { deleteAll(listOf(Stubs.projectDbm)) } doAnswer { }
    }
    creationDao.stub {
      onGeneric {
        findAllByData_CreatorUserIdAndData_CreatorProjectId(
          userId = Stubs.userId.userId,
          projectId = Stubs.userId.projectId,
        )
      } doReturn listOf(Stubs.projectCreationDbm)
    }

    assertThat { repository.removeAllProjectsByCreator(Stubs.userId) }.isSuccess()
    verify(userRepository).removeAllUsersByProjectId(Stubs.project.id)
    verify(projectDao).deleteAll(listOf(Stubs.projectDbm))
  }

}
