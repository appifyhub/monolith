package com.appifyhub.monolith.features.creator.repository

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.features.creator.storage.MessageTemplateDao
import com.appifyhub.monolith.features.creator.storage.model.MessageTemplateDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.util.Optional

class MessageTemplateRepositoryImplTest {

  private val messageTemplateDao = mock<MessageTemplateDao>()
  private val timeProvider = TimeProviderFake()

  private val repository: MessageTemplateRepository = MessageTemplateRepositoryImpl(
    templateDao = messageTemplateDao,
    timeProvider = timeProvider,
  )

  @Test fun `adding template works`() {
    messageTemplateDao.stub {
      onGeneric { save(any()) } doAnswer { Stubs.messageTemplateDbm }
    }

    assertThat(repository.addTemplate(Stubs.messageTemplateCreator))
      .isDataClassEqualTo(Stubs.messageTemplate)
  }

  @Test fun `fetching template by ID works`() {
    messageTemplateDao.stub {
      onGeneric { findById(any()) } doReturn Optional.of(Stubs.messageTemplateDbm)
    }

    assertThat(repository.fetchTemplateById(Stubs.messageTemplate.id))
      .isDataClassEqualTo(Stubs.messageTemplate)
  }

  @Test fun `fetching all templates by name works`() {
    messageTemplateDao.stub {
      onGeneric { findAllByProject_ProjectIdAndName(any(), any()) } doReturn listOf(Stubs.messageTemplateDbm)
    }

    assertThat(repository.fetchTemplatesByName(Stubs.project.id, Stubs.messageTemplate.name))
      .isEqualTo(listOf(Stubs.messageTemplate))
  }

  @Test fun `fetching all templates by project ID works`() {
    messageTemplateDao.stub {
      onGeneric { findAllByProject_ProjectId(any()) } doReturn listOf(Stubs.messageTemplateDbm)
    }

    assertThat(repository.fetchTemplatesByProjectId(Stubs.project.id))
      .isEqualTo(listOf(Stubs.messageTemplate))
  }

  @Test fun `fetching all templates by name and language works`() {
    messageTemplateDao.stub {
      onGeneric {
        findAllByProject_ProjectIdAndNameAndLanguageTag(
          projectId = any(),
          name = any(),
          languageTag = any(),
        )
      } doReturn listOf(Stubs.messageTemplateDbm)
    }

    assertThat(
      repository.fetchTemplatesByNameAndLanguage(
        Stubs.project.id,
        Stubs.messageTemplate.name,
        Stubs.messageTemplate.languageTag,
      ),
    ).isEqualTo(listOf(Stubs.messageTemplate))
  }

  @Test fun `updating template works`() {
    timeProvider.staticTime = { Stubs.messageTemplateUpdated.updatedAt.time }
    messageTemplateDao.stub {
      onGeneric { findById(any()) } doReturn Optional.of(Stubs.messageTemplateDbm)
      onGeneric { save(any()) } doAnswer { it.arguments.first() as MessageTemplateDbm }
    }

    assertThat(repository.updateTemplate(Stubs.messageTemplateUpdater))
      .isDataClassEqualTo(Stubs.messageTemplateUpdated)
  }

  @Test fun `deleting template by ID works`() {
    messageTemplateDao.stub {
      onGeneric { deleteById(any()) } doAnswer {}
    }

    assertAll {
      assertThat(repository.deleteTemplateById(Stubs.messageTemplate.id))
        .isEqualTo(Unit)
      verify(messageTemplateDao).deleteById(Stubs.messageTemplate.id)
    }
  }

  @Test fun `deleting all templates by project ID works`() {
    messageTemplateDao.stub {
      onGeneric { deleteAllByProject_ProjectId(any()) } doAnswer {}
    }

    assertAll {
      assertThat(repository.deleteAllTemplatesByProjectId(Stubs.project.id))
        .isEqualTo(Unit)
      verify(messageTemplateDao).deleteAllByProject_ProjectId(Stubs.project.id)
    }
  }

  @Test fun `deleting all templates by name works`() {
    messageTemplateDao.stub {
      onGeneric { deleteAllByProject_ProjectIdAndName(any(), any()) } doAnswer {}
    }

    assertAll {
      assertThat(repository.deleteAllTemplatesByName(Stubs.project.id, Stubs.messageTemplate.name))
        .isEqualTo(Unit)
      verify(messageTemplateDao).deleteAllByProject_ProjectIdAndName(Stubs.project.id, Stubs.messageTemplate.name)
    }
  }

}
