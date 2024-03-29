package com.appifyhub.monolith.features.creator.repository

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.features.user.domain.model.User.Authority.OWNER
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.creator.domain.model.ProjectUpdater
import com.appifyhub.monolith.features.creator.storage.ProjectCreationDao
import com.appifyhub.monolith.features.creator.storage.ProjectDao
import com.appifyhub.monolith.features.creator.storage.model.ProjectDbm
import com.appifyhub.monolith.features.user.repository.UserRepository
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.Date
import java.util.Optional

class CreatorRepositoryImplTest {

  private val projectDao = mock<ProjectDao>()
  private val creationDao = mock<ProjectCreationDao>()
  private val userRepository = mock<UserRepository>()
  private val messageTemplateRepository = mock<MessageTemplateRepository>()
  private val timeProvider = TimeProviderFake()

  private val repository = CreatorRepositoryImpl(
    projectDao = projectDao,
    creationDao = creationDao,
    userRepository = userRepository,
    messageTemplateRepository = messageTemplateRepository,
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

    verifyNoMoreInteractions(creationDao)
    assertThat(repository.addProject(Stubs.projectCreator))
      .isDataClassEqualTo(Stubs.project)
  }

  @Test fun `adding a project works (with creator)`() {
    // create & update times come from the stub
    val timesIterator = listOf(Stubs.project.createdAt, Stubs.project.updatedAt).iterator()
    val owner = Stubs.user.copy(id = UserId(userId = "uid", projectId = 100))
    timeProvider.staticTime = { timesIterator.next().time }
    SignatureGenerator.interceptor = { "signature" }

    assertThat(repository.addProject(Stubs.projectCreator.copy(owner = owner)))
      .isDataClassEqualTo(Stubs.project)
    verify(creationDao).save(anyVararg())
  }

  // Getting

  @Test fun `getting all creator projects works`() {
    assertThat(repository.fetchAllProjects())
      .isEqualTo(listOf(Stubs.project))
  }

  @Test fun `getting creator project works`() {
    assertThat(repository.getCreatorProject())
      .isDataClassEqualTo(Stubs.project)
  }

  @Test fun `getting creator owner works`() {
    userRepository.stub {
      onGeneric { fetchAllUsersByProjectId(Stubs.project.id) } doReturn listOf(Stubs.user.copy(authority = OWNER))
    }

    assertThat(repository.getSuperCreator())
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
          projectId = Stubs.userId.projectId,
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
        ),
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

    assertThat(repository.removeProjectById(Stubs.project.id)).isEqualTo(Unit)
    verify(userRepository).removeAllUsersByProjectId(Stubs.project.id)
    verify(creationDao).deleteAllByData_CreatedProjectId(Stubs.project.id)
    verify(messageTemplateRepository).deleteAllTemplatesByProjectId(Stubs.project.id)
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

    assertThat(repository.removeAllProjectsByCreator(Stubs.userId)).isEqualTo(Unit)
    verify(userRepository).removeAllUsersByProjectId(Stubs.project.id)
    verify(messageTemplateRepository).deleteAllTemplatesByProjectId(Stubs.project.id)
    verify(projectDao).deleteAll(listOf(Stubs.projectDbm))
  }

}
