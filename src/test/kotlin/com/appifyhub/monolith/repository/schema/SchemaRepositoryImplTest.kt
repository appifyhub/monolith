package com.appifyhub.monolith.repository.schema

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isFailure
import assertk.assertions.isFalse
import assertk.assertions.isSuccess
import assertk.assertions.isTrue
import assertk.assertions.messageContains
import com.appifyhub.monolith.storage.dao.SchemaDao
import com.appifyhub.monolith.storage.model.schema.SchemaDbm
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import java.util.Optional

class SchemaRepositoryImplTest {

  private val schemaDao = mock<SchemaDao>()

  private val repository: SchemaRepository = SchemaRepositoryImpl(
    schemaDao = schemaDao,
  )

  @BeforeEach fun setup() {
    schemaDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as SchemaDbm }
    }
  }

  // region Updates

  @Test fun `updating schema with a previous schema initialized fails`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.of(Stubs.schemaDbm)
    }

    assertThat { repository.update(Stubs.schema) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("already initialized")
      }
  }

  @Test fun `updating schema with a previous schema not initialized works`() {
    schemaDao.stub {
      val schema = SchemaDbm(Stubs.schema.version, isInitialized = false)
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.of(schema)
    }

    assertThat { repository.update(Stubs.schema) }
      .isSuccess()
  }

  @Test fun `updating schema with no previous schema works`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.empty()
    }

    assertThat { repository.update(Stubs.schema) }
      .isSuccess()
  }

  // endregion

  // region Is initialized checks

  @Test fun `fetching 'is initialized' returns false if DAO throws`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doThrow IllegalStateException("failed")
    }

    assertThat(repository.isInitialized(Stubs.schema.version))
      .isFalse()
  }

  @Test fun `fetching 'is initialized' returns false if schema is missing`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.empty()
    }

    assertThat(repository.isInitialized(Stubs.schema.version))
      .isFalse()
  }

  @Test fun `fetching 'is initialized' returns false for non-initialized schemas`() {
    schemaDao.stub {
      val schema = SchemaDbm(Stubs.schema.version, isInitialized = false)
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.of(schema)
    }

    assertThat(repository.isInitialized(Stubs.schema.version))
      .isFalse()
  }

  @Test fun `fetching 'is initialized' returns true for initialized schemas`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.of(Stubs.schemaDbm)
    }

    assertThat(repository.isInitialized(Stubs.schema.version))
      .isTrue()
  }

  // endregion

}
