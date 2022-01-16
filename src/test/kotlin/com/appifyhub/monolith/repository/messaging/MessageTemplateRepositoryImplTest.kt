package com.appifyhub.monolith.repository.messaging

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import com.appifyhub.monolith.storage.dao.MessageTemplateDao
import com.appifyhub.monolith.storage.model.messaging.MessageTemplateDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import java.util.Optional
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

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

  @Test fun `updating template works`() {
    messageTemplateDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as MessageTemplateDbm }
    }

    assertThat(repository.updateTemplate(Stubs.messageTemplate))
      .isDataClassEqualTo(Stubs.messageTemplate)
  }

  @Test fun `fetching template by ID works`() {
    messageTemplateDao.stub {
      onGeneric { findById(any()) } doReturn Optional.of(Stubs.messageTemplateDbm)
    }

    assertThat(repository.fetchTemplateById(Stubs.messageTemplate.id))
      .isDataClassEqualTo(Stubs.messageTemplate)
  }

  @Test fun `deleting template by ID works`() {
    messageTemplateDao.stub {
      onGeneric { deleteById(any()) } doAnswer {}
    }

    assertAll {
      assertThat { repository.deleteTemplateById(Stubs.messageTemplate.id) }
        .isSuccess()
      verify(messageTemplateDao).deleteById(Stubs.messageTemplate.id)
    }
  }

  @Test fun `fetching all templates by project ID works`() {
    messageTemplateDao.stub {
      onGeneric { findAllByProject_ProjectId(any()) } doReturn listOf(Stubs.messageTemplateDbm)
    }

    assertThat(repository.fetchAllTemplatesByProjectId(Stubs.project.id))
      .isEqualTo(listOf(Stubs.messageTemplate))
  }

  @Test fun `deleting all templates by project ID works`() {
    messageTemplateDao.stub {
      onGeneric { deleteAllByProject_ProjectId(any()) } doAnswer {}
    }

    assertAll {
      assertThat { repository.deleteAllTemplatesByProjectId(Stubs.project.id) }
        .isSuccess()
      verify(messageTemplateDao).deleteAllByProject_ProjectId(Stubs.project.id)
    }
  }

}
